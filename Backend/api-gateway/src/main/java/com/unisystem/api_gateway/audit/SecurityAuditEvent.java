package com.unisystem.api_gateway.audit;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

public record SecurityAuditEvent(
        UUID eventId, String eventType, int eventVersion, LocalDateTime occurredAt, String source,
        Long userId, String clientIp, String httpMethod, String requestPath, String correlationId,
        String aggregateId, Map<String, Object> event) {

    public static SecurityAuditEvent rateLimitExceeded(String clientIp, String method, String path,
                                                       String correlationId, String policy, int limit) {
        return new SecurityAuditEvent(UUID.randomUUID(), "RATE_LIMIT_EXCEEDED", 1,
                LocalDateTime.now(ZoneOffset.UTC), "api-gateway", null, clientIp, method, path,
                correlationId, clientIp, Map.of("policy", policy, "limit", limit, "windowSeconds", 60));
    }
}
