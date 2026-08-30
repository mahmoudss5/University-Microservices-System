package com.uni.iam.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record SecurityAuditEventDto(
        String eventId,
        String eventType,
        int eventVersion,
        LocalDateTime occurredAt,
        String source,
        Long userId,
        String clientIp,
        String httpMethod,
        String requestPath,
        String correlationId,
        String aggregateId,
        JsonNode event) {
}
