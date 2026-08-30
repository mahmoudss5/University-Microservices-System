package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.mapper;

import com.unisystem.academic_core_service.domain.model.OutboxEvent;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.OutboxEventEntity;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventPersistenceMapper {
    public OutboxEventEntity toEntity(OutboxEvent value) {
        return OutboxEventEntity.builder()
                .id(value.id()).eventId(value.eventId()).eventType(value.eventType())
                .aggregateId(value.aggregateId()).aggregateType(value.aggregateType())
                .eventData(value.eventData()).eventTime(value.eventTime()).eventStatus(value.status())
                .retryCount(value.retryCount()).nextAttemptAt(value.nextAttemptAt()).lastError(value.lastError())
                .claimedAt(value.claimedAt()).claimedBy(value.claimedBy()).processedAt(value.processedAt()).build();
    }

    public OutboxEvent toDomain(OutboxEventEntity entity) {
        return new OutboxEvent(entity.getId(), entity.getEventId(), entity.getEventType(), entity.getAggregateId(),
                entity.getAggregateType(), entity.getEventData(), entity.getEventTime(), entity.getEventStatus(),
                entity.getRetryCount(), entity.getNextAttemptAt(), entity.getLastError(), entity.getClaimedAt(),
                entity.getClaimedBy(), entity.getProcessedAt());
    }
}
