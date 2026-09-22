package com.AuditLog.AuditLogService.kafka;

import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditEventType;
import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditLog;
import com.AuditLog.AuditLogService.BusinessLogic.InterfaceServiceLayer.AuditLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuditLogKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditLogKafkaConsumer.class);

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public AuditLogKafkaConsumer(AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    /**
     * Each topic uses the same consumer group so every event is recorded once
     * by this service, independently from the other services' consumer groups.
     */
    @KafkaListener(
            topics = {
                    "security-audit-events.v1",
                    "user-registered-v1",
                    "user-updated-v1",
                    "user-deactivated-v1",
                    "user-deleted-v1",
                    "student-registered",
                    "student-enrolled",
                    "student-unenrolled",
                    "course-created",
                    "course-deleted",
                    "announcement-created",
                    "feedback-created",
                    "notification-push"
            },
            groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consume(ConsumerRecord<String, JsonNode> record) {
        AuditLog auditLog = toAuditLog(record);
        auditLogService.record(auditLog);
        log.debug("Processed audit event {} from topic {}", auditLog.getEventId(), record.topic());
    }

    private AuditLog toAuditLog(ConsumerRecord<String, JsonNode> record) {
        JsonNode message = record.value();
        if (message == null || !message.isObject()) {
            throw new IllegalArgumentException("Kafka audit event must be a JSON object");
        }

        JsonNode payload = message.path("event");
        if (payload.isMissingNode() || payload.isNull()) {
            payload = message;
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setEventId(eventId(message, record));
        auditLog.setEventType(eventType(message, record.topic()));
        auditLog.setEventVersion(message.path("eventVersion").asInt(1));
        auditLog.setOccurredAt(occurredAt(message));
        auditLog.setSource(source(message, record.topic()));
        auditLog.setUserId(firstLong(message, payload, "userId", "studentId", "recipientId"));
        auditLog.setClientIp(text(message, "clientIp"));
        auditLog.setHttpMethod(text(message, "httpMethod"));
        auditLog.setRequestPath(text(message, "requestPath"));
        auditLog.setCorrelationId(text(message, "correlationId"));
        auditLog.setAggregateId(aggregateId(message, payload, record));
        auditLog.setDetails(toJson(payload));
        return auditLog;
    }

    private String eventId(JsonNode message, ConsumerRecord<String, JsonNode> record) {
        String eventId = text(message, "eventId");
        if (eventId != null) {
            return eventId;
        }

        // IAM lifecycle events do not currently include an event ID. The Kafka
        // position makes the generated ID stable when the record is retried.
        String recordPosition = record.topic() + ':' + record.partition() + ':' + record.offset();
        return UUID.nameUUIDFromBytes(recordPosition.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private AuditEventType eventType(JsonNode message, String topic) {
        AuditEventType fromMessage = toEventType(text(message, "eventType"));
        if (fromMessage != AuditEventType.UNKNOWN) {
            return fromMessage;
        }

        return switch (topic) {
            case "user-registered-v1" -> AuditEventType.USER_REGISTERED;
            case "user-updated-v1" -> AuditEventType.USER_UPDATED;
            case "user-deactivated-v1" -> AuditEventType.USER_DEACTIVATED;
            case "user-deleted-v1" -> AuditEventType.USER_DELETED;
            case "student-registered" -> AuditEventType.STUDENT_REGISTERED;
            case "student-enrolled" -> AuditEventType.STUDENT_ENROLLED;
            case "student-unenrolled" -> AuditEventType.STUDENT_UNENROLLED;
            case "course-created" -> AuditEventType.COURSE_CREATED;
            case "course-deleted" -> AuditEventType.COURSE_DELETED;
            case "announcement-created" -> AuditEventType.ANNOUNCEMENT_CREATED;
            case "feedback-created" -> AuditEventType.FEEDBACK_CREATED;
            case "notification-push" -> AuditEventType.NOTIFICATION_PUSH;
            default -> AuditEventType.UNKNOWN;
        };
    }

    private AuditEventType toEventType(String value) {
        if (value == null || value.isBlank()) {
            return AuditEventType.UNKNOWN;
        }
        try {
            return AuditEventType.valueOf(value.toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException exception) {
            return AuditEventType.UNKNOWN;
        }
    }

    private LocalDateTime occurredAt(JsonNode message) {
        String value = text(message, "occurredAt");
        if (value == null) {
            return LocalDateTime.now(ZoneOffset.UTC);
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
            } catch (DateTimeParseException invalidTimestamp) {
                log.warn("Invalid audit event timestamp '{}'; using current UTC time", value);
                return LocalDateTime.now(ZoneOffset.UTC);
            }
        }
    }

    private String source(JsonNode message, String topic) {
        String source = text(message, "source");
        if (source != null) {
            return source;
        }

        if (topic.startsWith("user-") || topic.equals("student-registered")) {
            return "iam-service";
        }
        if (topic.equals("security-audit-events.v1")) {
            return "api-gateway";
        }
        if (topic.equals("notification-push")) {
            return "communication-service";
        }
        return "academic-core";
    }

    private Long firstLong(JsonNode message, JsonNode payload, String... fields) {
        for (String field : fields) {
            Long value = longValue(message, field);
            if (value != null) {
                return value;
            }
            value = longValue(payload, field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Long longValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.canConvertToLong() ? value.asLong() : null;
    }

    private String aggregateId(JsonNode message, JsonNode payload, ConsumerRecord<String, JsonNode> record) {
        String aggregateId = text(message, "aggregateId");
        if (aggregateId != null) {
            return aggregateId;
        }
        aggregateId = text(payload, "aggregateId");
        if (aggregateId != null) {
            return aggregateId;
        }
        if (record.key() != null && !record.key().isBlank()) {
            return record.key();
        }

        String[] fallbackFields = {"userId", "studentId", "courseId", "enrolledCourseId", "feedbackId", "recipientId"};
        for (String field : fallbackFields) {
            String value = text(payload, field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }

    private String toJson(JsonNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not serialize audit event payload", exception);
        }
    }
}
