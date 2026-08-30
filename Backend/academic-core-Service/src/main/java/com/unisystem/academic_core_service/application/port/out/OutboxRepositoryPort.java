package com.unisystem.academic_core_service.application.port.out;

import com.unisystem.academic_core_service.domain.model.OutboxEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

public interface OutboxRepositoryPort {
    OutboxEvent save(OutboxEvent event);
    List<OutboxEvent> claimPendingBatch(int batchSize, LocalDateTime now, LocalDateTime staleBefore, String claimedBy);
    void markProcessed(UUID eventId, LocalDateTime processedAt);
    void scheduleRetry(UUID eventId, int retryCount, LocalDateTime nextAttemptAt, String error);
    void markFailed(UUID eventId, int retryCount, String error);
}
