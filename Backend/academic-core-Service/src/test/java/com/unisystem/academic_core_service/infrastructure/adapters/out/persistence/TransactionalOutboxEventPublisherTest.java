package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.unisystem.academic_core_service.application.port.out.OutboxRepositoryPort;
import com.unisystem.academic_core_service.domain.events.CourseDeletedEvent;
import com.unisystem.academic_core_service.domain.model.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TransactionalOutboxEventPublisherTest {
    @Test void storesEnvelopeContainingActualEventPayload() throws Exception {
        OutboxRepositoryPort repository = mock(OutboxRepositoryPort.class);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        new TransactionalOutboxEventPublisher(repository, mapper).publishCourseDeleted(new CourseDeletedEvent("42"));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        OutboxEvent stored = captor.getValue();
        var json = mapper.readTree(stored.eventData());
        assertAll(
                () -> assertEquals(OutboxEvent.Status.PENDING, stored.status()),
                () -> assertEquals(OutboxEvent.EventType.COURSE_DELETED, stored.eventType()),
                () -> assertEquals("42", json.path("aggregateId").asText()),
                () -> assertEquals("42", json.path("event").path("courseId").asText()));
    }
}
