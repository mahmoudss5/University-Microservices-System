package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence;

import com.unisystem.academic_core_service.application.port.out.OutboxRepositoryPort;
import com.unisystem.academic_core_service.domain.model.OutboxEvent;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.mapper.OutboxEventPersistenceMapper;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository.OutboxEventJpaRepository;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.OutboxEventEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxPersistenceAdapter implements OutboxRepositoryPort {
    private final OutboxEventJpaRepository repository;
    private final OutboxEventPersistenceMapper mapper;

    @Override public OutboxEvent save(OutboxEvent event) { return mapper.toDomain(repository.save(mapper.toEntity(event))); }

    @Override
    @Transactional
    public List<OutboxEvent> claimPendingBatch(int batchSize, LocalDateTime now, LocalDateTime staleBefore, String claimedBy) {
        var entities = repository.lockPublishableBatch(batchSize, now, staleBefore);
        entities.forEach(entity -> {
            entity.setEventStatus(OutboxEvent.Status.PROCESSING);
            entity.setClaimedAt(now);
            entity.setClaimedBy(claimedBy);
        });
        return repository.saveAll(entities).stream().map(mapper::toDomain).toList();
    }

    @Override @Transactional
    public void markProcessed(UUID eventId, LocalDateTime processedAt) {
        var entity = require(eventId);
        entity.setEventStatus(OutboxEvent.Status.PROCESSED);
        entity.setProcessedAt(processedAt);
        entity.setClaimedAt(null); entity.setClaimedBy(null); entity.setLastError(null);
    }

    @Override @Transactional
    public void scheduleRetry(UUID eventId, int retryCount, LocalDateTime nextAttemptAt, String error) {
        var entity = require(eventId);
        entity.setEventStatus(OutboxEvent.Status.PENDING);
        entity.setRetryCount(retryCount); entity.setNextAttemptAt(nextAttemptAt); entity.setLastError(error);
        entity.setClaimedAt(null); entity.setClaimedBy(null);
    }

    @Override @Transactional
    public void markFailed(UUID eventId, int retryCount, String error) {
        var entity = require(eventId);
        entity.setEventStatus(OutboxEvent.Status.FAILED);
        entity.setRetryCount(retryCount); entity.setLastError(error);
        entity.setNextAttemptAt(LocalDateTime.of(9999, 12, 31, 23, 59));
        entity.setClaimedAt(null); entity.setClaimedBy(null);
    }

    private OutboxEventEntity require(UUID eventId) {
        return repository.findByEventId(eventId).orElseThrow(() -> new IllegalStateException("Outbox event not found: " + eventId));
    }
}
