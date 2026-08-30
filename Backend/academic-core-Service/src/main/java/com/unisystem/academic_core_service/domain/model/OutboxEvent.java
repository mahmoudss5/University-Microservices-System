package com.unisystem.academic_core_service.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record OutboxEvent(
        Long id,
        UUID eventId,
        EventType eventType,
        String aggregateId,
        String aggregateType,
        String eventData,
        LocalDateTime eventTime,
        Status status,
        int retryCount,
        LocalDateTime nextAttemptAt,
        String lastError,
        LocalDateTime claimedAt,
        String claimedBy,
        LocalDateTime processedAt) {

    public enum Status { PENDING, PROCESSING, PROCESSED, FAILED }
    public enum EventType {
        COURSE_CREATED, COURSE_DELETED, STUDENT_ENROLLED, STUDENT_UNENROLLED,
        FEEDBACK_CREATED, ANNOUNCEMENT_CREATED
    }
}
