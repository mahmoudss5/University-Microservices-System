package com.unisystem.api_gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Rate Limiter Configuration
 * ──────────────────────────
 * Registers the shared infrastructure beans used by both the global
 * sliding-window filter and the per-route Lua rate-limiter filter factory.
 *
 * Bean summary
 * ────────────
 * ipKeyResolver         – resolves the client key from the remote IP address.
 * rateLimiterRedisScript – loads rate_limiter.lua from the classpath once and
 *                          caches its SHA1 digest for EVALSHA calls.
 *
 * The ReactiveRedisTemplate<String, String> is auto-configured by Spring Boot
 * when spring-boot-starter-data-redis-reactive is on the classpath, so we
 * don't need to declare it explicitly here.
 */
@Configuration
public class RateLimiterConfig {

    // ── Key Resolver ─────────────────────────────────────────────────────────

    /**
     * Resolves the rate-limit bucket key for a request.
     * Falls back to "anonymous" when the remote address is absent
     * (e.g., behind certain reverse proxies before X-Forwarded-For is mapped).
     *
     * The bean name ipKeyResolver is referenced in application.properties via
     * the SpEL expression #{@ipKeyResolver}.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            var addr = exchange.getRequest().getRemoteAddress();
            String key = (addr != null)
                    ? addr.getAddress().getHostAddress()
                    : "anonymous";
            return Mono.just(key);
        };
    }

    // ── Redis Lua Script Bean ─────────────────────────────────────────────────

    /**
     * Loads the sliding-window Lua script from the classpath and wraps it in a
     * DefaultRedisScript.  Spring Data Redis will use EVALSHA on subsequent calls
     * so the script is uploaded to Redis only once — all later executions use the
     * cached SHA1.
     *
     * Return type  List<Long>:
     *   index 0 → 1 (allowed) or 0 (denied)
     *   index 1 → remaining requests in the current window
     */
    @Bean
    public DefaultRedisScript<List> rateLimiterRedisScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("scripts/rate_limiter.lua"))
        );
        script.setResultType(List.class);
        return script;
    }
}
