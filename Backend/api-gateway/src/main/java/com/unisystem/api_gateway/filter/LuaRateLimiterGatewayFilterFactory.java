package com.unisystem.api_gateway.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * LuaRateLimiter — Configurable Per-Route Sliding-Window Rate Limiter
 * ─────────────────────────────────────────────────────────────────────
 * A Spring Cloud Gateway filter factory that enforces a per-IP, per-route
 * sliding-window rate limit using our custom Lua script running in Redis.
 *
 * Usage in application.properties
 * ────────────────────────────────
 * spring.cloud.gateway.routes[N].filters[0].name=LuaRateLimiter
 * spring.cloud.gateway.routes[N].filters[0].args.requestsPerMinute=20
 *
 * Config fields
 * ─────────────
 *   requestsPerMinute  – max calls allowed per 60-second sliding window (default 20)
 *
 * Redis key structure
 * ───────────────────
 *   rl:route:{routeId}:{clientIp}
 *
 * Response headers (on allow)
 * ───────────────────────────
 *   X-RateLimit-Limit     – configured requestsPerMinute
 *   X-RateLimit-Remaining – slots left in the current window
 *   X-RateLimit-Window    – window in seconds (always 60)
 *
 * Response on deny  →  HTTP 429 + Retry-After: 60
 */
@Slf4j
@Component
public class LuaRateLimiterGatewayFilterFactory
        extends AbstractGatewayFilterFactory<LuaRateLimiterGatewayFilterFactory.Config> {

    private static final long   WINDOW_MS  = 60_000L;  // 1-minute sliding window
    private static final String KEY_PREFIX = "rl:route:";

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final DefaultRedisScript<List>              rateLimiterRedisScript;

    public LuaRateLimiterGatewayFilterFactory(
            ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
            DefaultRedisScript<List>              rateLimiterRedisScript) {

        super(Config.class);
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.rateLimiterRedisScript = rateLimiterRedisScript;
    }

    // ── Config POJO ──────────────────────────────────────────────────────────

    /**
     * Bound from application.properties args.requestsPerMinute.
     * Default: 20 requests / minute (suitable for auth endpoints).
     */
    @Getter
    @Setter
    public static class Config {
        private int requestsPerMinute = 20;
    }

    // ── Filter creation ──────────────────────────────────────────────────────

    @Override
    @SuppressWarnings("unchecked")
    public GatewayFilter apply(Config config) {
        int limit = config.getRequestsPerMinute();

        return (exchange, chain) -> {

            // ── 1. Client key: routeId + remote IP ───────────────────────────
            String routeId = exchange.getAttribute(
                    org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR) != null
                    ? ((org.springframework.cloud.gateway.route.Route)
                       exchange.getAttribute(
                               org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR))
                      .getId()
                    : "unknown";

            var remoteAddr = exchange.getRequest().getRemoteAddress();
            String clientIp = (remoteAddr != null)
                    ? remoteAddr.getAddress().getHostAddress()
                    : "anonymous";

            String redisKey  = KEY_PREFIX + routeId + ":" + clientIp;
            long   nowMs     = Instant.now().toEpochMilli();
            String requestId = UUID.randomUUID().toString();

            // ── 2. Execute Lua script ─────────────────────────────────────────
            return reactiveRedisTemplate
                    .execute(
                            rateLimiterRedisScript,
                            List.of(redisKey),
                            List.of(
                                    String.valueOf(WINDOW_MS),
                                    String.valueOf(limit),
                                    String.valueOf(nowMs),
                                    requestId
                            )
                    )
                    .next()
                    .flatMap(result -> {
                        List<Long> rl        = (List<Long>) result;
                        long       allowed    = rl.get(0);
                        long       remaining  = rl.get(1);

                        if (allowed == 1L) {
                            exchange.getResponse().getHeaders()
                                    .add("X-RateLimit-Limit",     String.valueOf(limit));
                            exchange.getResponse().getHeaders()
                                    .add("X-RateLimit-Remaining", String.valueOf(remaining));
                            exchange.getResponse().getHeaders()
                                    .add("X-RateLimit-Window",    "60");

                            return chain.filter(exchange);
                        } else {
                            log.warn("[LuaRateLimit] Route '{}' — IP {} exceeded {} req/min",
                                    routeId, clientIp, limit);

                            var response = exchange.getResponse();
                            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            response.getHeaders().add("Retry-After",           "60");
                            response.getHeaders().add("X-RateLimit-Limit",     String.valueOf(limit));
                            response.getHeaders().add("X-RateLimit-Remaining", "0");
                            response.getHeaders().add("X-RateLimit-Window",    "60");
                            return response.setComplete();
                        }
                    })
                    // Fail open on Redis errors — never let a cache outage block auth
                    .onErrorResume(ex -> {
                        log.error("[LuaRateLimit] Redis error on route '{}' for IP {}: {} — failing open",
                                routeId, clientIp, ex.getMessage());
                        return chain.filter(exchange);
                    });
        };
    }
}
