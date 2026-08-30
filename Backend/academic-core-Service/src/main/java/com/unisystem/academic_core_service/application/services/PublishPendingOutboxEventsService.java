package com.unisystem.academic_core_service.application.services;

import com.unisystem.academic_core_service.application.port.in.PublishPendingOutboxEventsUseCase;
import com.unisystem.academic_core_service.application.port.out.MessageBrokerPort;
import com.unisystem.academic_core_service.application.port.out.OutboxRepositoryPort;
import java.net.InetAddress;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PublishPendingOutboxEventsService implements PublishPendingOutboxEventsUseCase {
    private final OutboxRepositoryPort repository;
    private final MessageBrokerPort broker;
    private final int batchSize;
    private final int maxRetries;
    private final long claimTimeoutSeconds;
    private final String workerId;

    public PublishPendingOutboxEventsService(OutboxRepositoryPort repository, MessageBrokerPort broker,
                                             int batchSize, int maxRetries, long claimTimeoutSeconds) {
        this.repository = repository;
        this.broker = broker;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
        this.claimTimeoutSeconds = claimTimeoutSeconds;
        this.workerId = resolveWorkerId();
    }

    @Override
    public void publishBatch() {
        LocalDateTime now = LocalDateTime.now();
        var events = repository.claimPendingBatch(batchSize, now, now.minusSeconds(claimTimeoutSeconds), workerId);
        for (var event : events) {
            try {
                broker.publish(event);
                repository.markProcessed(event.eventId(), LocalDateTime.now());
            } catch (Exception exception) {
                int attempts = event.retryCount() + 1;
                String error = abbreviate(exception.getMessage());
                if (attempts >= maxRetries) {
                    repository.markFailed(event.eventId(), attempts, error);
                    log.error("Outbox event permanently failed eventId={} type={} attempts={}", event.eventId(), event.eventType(), attempts, exception);
                } else {
                    repository.scheduleRetry(event.eventId(), attempts, LocalDateTime.now().plusSeconds(backoffSeconds(attempts)), error);
                    log.warn("Outbox event scheduled for retry eventId={} attempt={}", event.eventId(), attempts);
                }
            }
        }
    }
    private long backoffSeconds(int attempt) { return Math.min(600L, 1L << Math.min(attempt - 1, 9)); }
    private String abbreviate(String value) { return value == null ? "Unknown broker failure" : value.substring(0, Math.min(value.length(), 2000)); }
    private String resolveWorkerId() {
        try { return InetAddress.getLocalHost().getHostName() + "-" + ProcessHandle.current().pid(); }
        catch (Exception ignored) { return "academic-core-" + ProcessHandle.current().pid(); }
    }
}
