package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EventDtoTest {
    @Test void wrapsTheActualEventPayloadAndMetadata() {
        var payload = new CourseDeletedEventDto("42");
        var envelope = EventDto.create("COURSE_DELETED", "42", payload);
        assertAll(
                () -> assertNotNull(envelope.eventId()),
                () -> assertEquals(1, envelope.eventVersion()),
                () -> assertEquals("COURSE_DELETED", envelope.eventType()),
                () -> assertEquals("42", envelope.aggregateId()),
                () -> assertSame(payload, envelope.event()));
    }
}
