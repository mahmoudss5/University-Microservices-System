package com.unisystem.api_gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
public class InternalRequestHeadersFactory {

    private final String jwtSecret;

    public InternalRequestHeadersFactory(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public InternalRequestHeaders create(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }

        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(rawToken)
                    .getBody();
            return new InternalRequestHeaders(
                    token,
                    stringValue(claims.get("userId")),
                    extractRoleValue(claims.get("roles")));
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn("BFF: Failed to parse JWT for internal headers: {}", exception.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private String extractRoleValue(Object roles) {
        if (roles == null) {
            return "";
        }
        if (roles instanceof List<?> list) {
            return list.isEmpty() ? "" : normalizeRoleHeader(stringValue(list.getFirst()));
        }
        if (roles instanceof Object[] array) {
            return array.length == 0 ? "" : normalizeRoleHeader(stringValue(array[0]));
        }
        return normalizeRoleHeader(roles.toString());
    }

    private String normalizeRoleHeader(String rawRoles) {
        String cleaned = rawRoles == null ? "" : rawRoles.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        String first = cleaned.split("[,\\s]+", 2)[0].trim();
        if (first.isEmpty()) {
            return "";
        }
        return first.startsWith("ROLE_") ? first : "ROLE_" + first.toUpperCase(Locale.ROOT);
    }

    public record InternalRequestHeaders(String authorization, String userId, String roles) {
    }
}
