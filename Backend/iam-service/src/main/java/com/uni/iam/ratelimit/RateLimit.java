package com.uni.iam.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @RateLimit} — Sliding-Window Rate Limiting Annotation
 * ─────────────────────────────────────────────────────────────
 * Apply this annotation to any Spring MVC controller method (or at the class
 * level to apply to all methods) to enforce a per-client-IP, sliding-window
 * request cap backed by Redis.
 *
 * <p>The limit is evaluated <em>after</em> the API Gateway's own rate limiter,
 * giving a two-layer defence-in-depth strategy:
 * <ol>
 *   <li>Gateway Layer 1  — 150 req/min per IP on all routes (GlobalRateLimiterFilter)</li>
 *   <li>Gateway Layer 2  — 20 req/min per IP on /api/auth/** (LuaRateLimiterGatewayFilterFactory)</li>
 *   <li><strong>Service Layer</strong> — {@code requestsPerMinute} req/min per IP
 *       enforced here, inside the IAM service itself, by {@link RateLimitAspect}</li>
 * </ol>
 *
 * <h3>Usage examples</h3>
 * <pre>{@code
 * // Restrict a single endpoint to 20 calls per minute per IP
 * @PostMapping("/login")
 * @RateLimit(requestsPerMinute = 20)
 * public ResponseEntity<AuthResponse> login(...) { ... }
 *
 * // Restrict all methods in a controller to 10 calls per minute
 * @RateLimit(requestsPerMinute = 10)
 * @RestController
 * public class SensitiveController { ... }
 * }</pre>
 *
 * <h3>Behaviour when the limit is exceeded</h3>
 * <ul>
 *   <li>HTTP 429 Too Many Requests is returned.</li>
 *   <li>Response headers {@code X-RateLimit-Limit}, {@code X-RateLimit-Remaining},
 *       and {@code Retry-After: 60} are set.</li>
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * Maximum number of requests allowed per client IP within a 60-second
     * sliding window.  Defaults to 20, which is suitable for authentication
     * and registration endpoints.
     *
     * @return the per-minute request cap (must be &gt; 0)
     */
    int requestsPerMinute() default 20;
}
