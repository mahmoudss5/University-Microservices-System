package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** Stable integration envelope; event contains the versioned event-specific DTO. */
public record EventDto<T>(
        UUID eventId,
        String eventType,
        int eventVersion,
        LocalDateTime occurredAt,
        String aggregateId,
        T event) {

    public EventDto {
        if (eventId == null) throw new IllegalArgumentException("eventId is required");
        if (eventType == null || eventType.isBlank()) throw new IllegalArgumentException("eventType is required");
        if (eventVersion < 1) throw new IllegalArgumentException("eventVersion must be positive");
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (aggregateId == null || aggregateId.isBlank()) throw new IllegalArgumentException("aggregateId is required");
        if (event == null) throw new IllegalArgumentException("event payload is required");
    }

    public static <T> EventDto<T> create(String eventType, String aggregateId, T event) {
        return new EventDto<>(UUID.randomUUID(), eventType, 1, LocalDateTime.now(), aggregateId, event);
    }
}
