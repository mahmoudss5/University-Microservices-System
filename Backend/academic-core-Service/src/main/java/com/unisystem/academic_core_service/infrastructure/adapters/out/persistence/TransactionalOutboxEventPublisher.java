package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unisystem.academic_core_service.application.port.out.EventPublisherPort;
import com.unisystem.academic_core_service.application.port.out.OutboxRepositoryPort;
import com.unisystem.academic_core_service.domain.events.*;
import com.unisystem.academic_core_service.domain.model.OutboxEvent;
import com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.dto.EventDto;
import com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.dto.EventDtoMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionalOutboxEventPublisher implements EventPublisherPort {
    private final OutboxRepositoryPort repository;
    private final ObjectMapper objectMapper;

    @Override public void publishStudentEnrolled(StudentEnrollend event) {
        record(OutboxEvent.EventType.STUDENT_ENROLLED, event.studentId(), "ENROLLMENT", EventDtoMapper.toDto(event));
    }
    @Override public void publishStudentUnenrolled(StudentUnenrolledEvent event) {
        record(OutboxEvent.EventType.STUDENT_UNENROLLED, event.studentId(), "ENROLLMENT", EventDtoMapper.toDto(event));
    }
    @Override public void publishCourseCreated(CourseCreatedEvent event) {
        record(OutboxEvent.EventType.COURSE_CREATED, event.courseId(), "COURSE", EventDtoMapper.toDto(event));
    }
    @Override public void publishCourseDeleted(CourseDeletedEvent event) {
        record(OutboxEvent.EventType.COURSE_DELETED, event.courseId(), "COURSE", EventDtoMapper.toDto(event));
    }
    @Override public void publishAnnouncementCreated(AnnouncementCreatedEvent event) {
        record(OutboxEvent.EventType.ANNOUNCEMENT_CREATED, event.id(), "ANNOUNCEMENT", EventDtoMapper.toDto(event));
    }
    @Override public void publishFeedbackCreated(FeedbackCreatedEvent event) {
        record(OutboxEvent.EventType.FEEDBACK_CREATED, event.feedbackId(), "FEEDBACK", EventDtoMapper.toDto(event));
    }

    private void record(OutboxEvent.EventType type, String aggregateId, String aggregateType, Object payload) {
        EventDto<Object> envelope = EventDto.create(type.name(), aggregateId, payload);
        try {
            repository.save(new OutboxEvent(null, envelope.eventId(), type, aggregateId, aggregateType,
                    objectMapper.writeValueAsString(envelope), envelope.occurredAt(), OutboxEvent.Status.PENDING,
                    0, envelope.occurredAt(), null, null, null, null));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize outbox event " + type, exception);
        }
    }
}
