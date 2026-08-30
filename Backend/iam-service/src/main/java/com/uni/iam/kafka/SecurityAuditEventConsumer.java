package com.uni.iam.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.uni.iam.dto.request.SecurityAuditEventDto;
import com.uni.iam.service.interfaces.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SecurityAuditEventConsumer {
    private final SecurityAuditService auditService;

    @KafkaListener(topics = "security-audit-events.v1", groupId = "iam-security-audit-v1")
    public void consumeGatewayAudit(JsonNode message) {
        auditService.record(toAudit(message, requiredText(message, "source")));
    }

    @KafkaListener(
            topics = {"student-enrolled", "student-unenrolled", "course-created", "course-deleted",
                    "announcement-created", "feedback-created"},
            groupId = "iam-academic-audit-v1")
    public void consumeAcademicAudit(JsonNode message) {
        auditService.record(toAudit(message, "academic-core"));
    }

    private SecurityAuditEventDto toAudit(JsonNode message, String source) {
        JsonNode payload = message.path("event");
        Long userId = firstLong(payload, "studentId", "userId");
        return new SecurityAuditEventDto(
                requiredText(message, "eventId"), requiredText(message, "eventType"),
                message.path("eventVersion").asInt(), LocalDateTime.parse(requiredText(message, "occurredAt")),
                source, userId, nullableText(message, "clientIp"), nullableText(message, "httpMethod"),
                nullableText(message, "requestPath"), nullableText(message, "correlationId"),
                nullableText(message, "aggregateId"), payload.isMissingNode() ? null : payload);
    }

    private String requiredText(JsonNode node, String field) {
        String value = nullableText(node, field);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    private String nullableText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private Long firstLong(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && value.canConvertToLong()) return value.asLong();
        }
        return null;
    }
}
