package com.uni.iam.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;

/**
 * RateLimitAspect — AOP Interceptor for {@link RateLimit}
 * ─────────────────────────────────────────────────────────
 * Intercepts any Spring bean method annotated with {@code @RateLimit} and
 * enforces a <b>sliding-window</b> rate limit backed by a Redis Sorted Set.
 *
 * <h3>Algorithm (same Lua strategy, plain Java + Redis pipeline)</h3>
 * <ol>
 *   <li>Determine the client IP from the current HTTP request
 *       (honours {@code X-Forwarded-For} set by the API Gateway).</li>
 *   <li>Build a bucket key:  {@code rl:service:{className}.{methodName}:{clientIp}}</li>
 *   <li>Remove all sorted-set members whose score (epoch-ms) is older than
 *       the 60-second window.</li>
 *   <li>Count the remaining members.</li>
 *   <li>If {@code count < limit} → add the current request, proceed.</li>
 *   <li>Otherwise → write a 429 response with {@code Retry-After: 60} and
 *       short-circuit the method call.</li>
 * </ol>
 *
 * <h3>Redis key structure</h3>
 * <pre>
 *   rl:service:AuthController.login:192.168.1.10
 *   rl:service:AuthController.register:10.0.0.2
 * </pre>
 *
 * <h3>Annotation resolution priority</h3>
 * Method-level {@code @RateLimit} takes precedence over class-level.
 * If neither is present the aspect does nothing (it shouldn't be invoked,
 * but this guard prevents accidents with wildcard pointcuts).
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final long   WINDOW_MS  = 60_000L;   // 1-minute sliding window
    private static final String KEY_PREFIX = "rl:service:";

    /** Plain (blocking) StringRedisTemplate — IAM service is a Servlet/MVC app, not WebFlux. */
    private final StringRedisTemplate redisTemplate;

    // ── Pointcut + advice ────────────────────────────────────────────────────

    /**
     * Around advice that intercepts any method (or any method in a class)
     * annotated with {@code @RateLimit}.
     *
     * <p>The pointcut matches both forms:
     * <ul>
     *   <li>{@code @annotation(com.uni.iam.ratelimit.RateLimit)} — method-level</li>
     *   <li>{@code @within(com.uni.iam.ratelimit.RateLimit)}     — class-level</li>
     * </ul>
     */
    @Around("@annotation(com.uni.iam.ratelimit.RateLimit) || @within(com.uni.iam.ratelimit.RateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {

        // ── 1. Resolve the effective @RateLimit annotation ───────────────────
        RateLimit annotation = resolveAnnotation(joinPoint);
        if (annotation == null) {
            // Safety net: no annotation found — proceed without limiting
            return joinPoint.proceed();
        }
        int limit = annotation.requestsPerMinute();

        // ── 2. Obtain the current HTTP context ───────────────────────────────
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            // Not inside an HTTP request (e.g., async or test context) — skip
            return joinPoint.proceed();
        }

        HttpServletRequest  request  = attrs.getRequest();
        HttpServletResponse response = attrs.getResponse();

        // ── 3. Determine client IP ────────────────────────────────────────────
        // Prefer X-Forwarded-For set by the API Gateway (it strips client spoofing).
        String clientIp = resolveClientIp(request);

        // ── 4. Build Redis bucket key ─────────────────────────────────────────
        String className  = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String redisKey   = KEY_PREFIX + className + "." + methodName + ":" + clientIp;

        // ── 5. Sliding-window check via Redis ────────────────────────────────
        long   nowMs      = Instant.now().toEpochMilli();
        long   windowStart = nowMs - WINDOW_MS;
        String requestId  = UUID.randomUUID().toString();

        // a) Prune entries outside the sliding window
        redisTemplate.opsForZSet().removeRangeByScore(redisKey, Double.NEGATIVE_INFINITY, windowStart);

        // b) Count requests still inside the window
        Long currentCount = redisTemplate.opsForZSet().zCard(redisKey);
        long count = (currentCount != null) ? currentCount : 0L;

        if (count >= limit) {
            // ── Denied: return 429 ────────────────────────────────────────────
            log.warn("[RateLimitAspect] {}.{}() — IP {} exceeded {} req/min (current: {})",
                    className, methodName, clientIp, limit, count);

            if (response != null) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After",           "60");
                response.setHeader("X-RateLimit-Limit",     String.valueOf(limit));
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Window",    "60");
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"status\":429,\"error\":\"Too Many Requests\"," +
                        "\"message\":\"Rate limit exceeded. Max " + limit +
                        " requests per minute. Retry after 60 seconds.\"}"
                );
                response.getWriter().flush();
            }
            return null;
        }

        // c) Allowed: record this request + refresh TTL
        redisTemplate.opsForZSet().add(redisKey, requestId, nowMs);
        redisTemplate.expire(redisKey, java.time.Duration.ofMillis(WINDOW_MS));

        // ── Allowed: annotate response with rate-limit headers ────────────────
        long remaining = limit - count - 1;
        if (response != null) {
            response.setHeader("X-RateLimit-Limit",     String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(remaining, 0)));
            response.setHeader("X-RateLimit-Window",    "60");
        }

        log.debug("[RateLimitAspect] {}.{}() — IP {} allowed ({}/{} req/min)",
                className, methodName, clientIp, count + 1, limit);

        return joinPoint.proceed();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Resolves the {@code @RateLimit} annotation, preferring method-level
     * over class-level so individual methods can override the class default.
     */
    private RateLimit resolveAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature sig    = (MethodSignature) joinPoint.getSignature();
        Method          method = sig.getMethod();

        // Method-level takes priority
        RateLimit methodAnnotation = method.getAnnotation(RateLimit.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // Fall back to class-level
        return joinPoint.getTarget().getClass().getAnnotation(RateLimit.class);
    }

    /**
     * Extracts the real client IP.
     * <ul>
     *   <li>Uses {@code X-Forwarded-For} if present (set and validated by
     *       the API Gateway's JwtAuthFilter before it strips client-supplied headers).</li>
     *   <li>Falls back to {@code request.getRemoteAddr()} otherwise.</li>
     * </ul>
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For may be a comma-separated chain; the first entry is the client
            return forwarded.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr : "anonymous";
    }
}
