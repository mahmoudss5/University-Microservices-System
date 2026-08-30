package com.unisystem.api_gateway.filter;

import com.unisystem.api_gateway.audit.SecurityAuditEvent;
import com.unisystem.api_gateway.audit.SecurityAuditPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Global Sliding-Window Rate Limiter — 150 requests / minute / client IP
 * ────────────────────────────────────────────────────────────────────────
 * This filter sits at order -2 (before the JWT filter at -1) and applies a
 * broad-traffic guard to *every* incoming request, regardless of route.
 *
 * Algorithm
 * ─────────
 * A Redis Sorted-Set keyed by "rl:global:{clientIp}" is managed atomically
 * by the Lua script at classpath:scripts/rate_limiter.lua.  The script:
 *   1. Removes entries older than the 60-second sliding window.
 *   2. Counts remaining entries.
 *   3. Allows the request (adds an entry) or rejects it.
 *
 * Response headers when allowed
 * ─────────────────────────────
 *   X-RateLimit-Limit     – max requests per window (150)
 *   X-RateLimit-Remaining – tokens left in the current window
 *   X-RateLimit-Window    – window size in seconds (60)
 *
 * Response when denied  →  HTTP 429 Too Many Requests
 *   Retry-After           – seconds until a slot frees up (≈ window size)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalRateLimiterFilter implements GlobalFilter, Ordered {

    private static final int  REQUESTS_PER_MINUTE  = 150;
    private static final long WINDOW_MS             = 60_000L;   // 1 minute in ms
    private static final String KEY_PREFIX          = "rl:global:";

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final DefaultRedisScript<List>              rateLimiterRedisScript;
    private final SecurityAuditPublisher auditPublisher;

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // ── 1. Resolve the client key (remote IP) ────────────────────────────
        var remoteAddr = exchange.getRequest().getRemoteAddress();
        String clientIp = (remoteAddr != null)
                ? remoteAddr.getAddress().getHostAddress()
                : "anonymous";

        String redisKey   = KEY_PREFIX + clientIp;
        long   nowMs      = Instant.now().toEpochMilli();
        String requestId  = UUID.randomUUID().toString();

        // ── 2. Execute the Lua script atomically in Redis ────────────────────
        return reactiveRedisTemplate
                .execute(
                        rateLimiterRedisScript,
                        List.of(redisKey),                            // KEYS
                        List.of(                                      // ARGV
                                String.valueOf(WINDOW_MS),
                                String.valueOf(REQUESTS_PER_MINUTE),
                                String.valueOf(nowMs),
                                requestId
                        )
                )
                .next()   // the script returns a single list result
                .flatMap(result -> {
                    List<Long> rl       = (List<Long>) result;
                    long       allowed   = rl.get(0);
                    long       remaining = rl.get(1);

                    if (allowed == 1L) {
                        // ── Allowed: annotate the response with rate-limit headers
                        exchange.getResponse().getHeaders()
                                .add("X-RateLimit-Limit",     String.valueOf(REQUESTS_PER_MINUTE));
                        exchange.getResponse().getHeaders()
                                .add("X-RateLimit-Remaining", String.valueOf(remaining));
                        exchange.getResponse().getHeaders()
                                .add("X-RateLimit-Window",    "60");

                        return chain.filter(exchange);
                    } else {
                        // ── Denied: return 429 Too Many Requests
                        log.warn("[GlobalRateLimit] IP {} exceeded {} req/min limit",
                                clientIp, REQUESTS_PER_MINUTE);
                        auditPublisher.publish(SecurityAuditEvent.rateLimitExceeded(
                                clientIp, exchange.getRequest().getMethod().name(),
                                exchange.getRequest().getURI().getPath(), exchange.getRequest().getId(),
                                "global-ip", REQUESTS_PER_MINUTE));

                        var response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        response.getHeaders().add("Retry-After",           "60");
                        response.getHeaders().add("X-RateLimit-Limit",     String.valueOf(REQUESTS_PER_MINUTE));
                        response.getHeaders().add("X-RateLimit-Remaining", "0");
                        response.getHeaders().add("X-RateLimit-Window",    "60");
                        return response.setComplete();
                    }
                })
                // Fallback: if Redis is unreachable, fail open (let the request through)
                // to avoid a Redis outage taking down the entire gateway.
                .onErrorResume(ex -> {
                    log.error("[GlobalRateLimit] Redis error for IP {}: {} — failing open",
                            clientIp, ex.getMessage());
                    return chain.filter(exchange);
                });
    }

    /**
     * Order -2: runs BEFORE the JWT auth filter (order -1).
     * Rate-limit headers are the outermost concern — we reject abusive
     * traffic before spending any CPU on token validation.
     */
    @Override
    public int getOrder() {
        return -2;
    }
}
