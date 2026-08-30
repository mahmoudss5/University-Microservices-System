package com.unisystem.academic_core_service.application.services;

import static org.mockito.Mockito.*;
import com.unisystem.academic_core_service.application.port.out.MessageBrokerPort;
import com.unisystem.academic_core_service.application.port.out.OutboxRepositoryPort;
import com.unisystem.academic_core_service.domain.model.OutboxEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PublishPendingOutboxEventsServiceTest {
    private final OutboxRepositoryPort repository = mock(OutboxRepositoryPort.class);
    private final MessageBrokerPort broker = mock(MessageBrokerPort.class);

    @Test void marksAcknowledgedEventProcessed() throws Exception {
        OutboxEvent event = event(0);
        when(repository.claimPendingBatch(anyInt(), any(), any(), anyString())).thenReturn(List.of(event));
        new PublishPendingOutboxEventsService(repository, broker, 10, 3, 60).publishBatch();
        verify(broker).publish(event);
        verify(repository).markProcessed(eq(event.eventId()), any());
    }

    @Test void schedulesBrokerFailureForRetry() throws Exception {
        OutboxEvent event = event(0);
        when(repository.claimPendingBatch(anyInt(), any(), any(), anyString())).thenReturn(List.of(event));
        doThrow(new RuntimeException("Kafka unavailable")).when(broker).publish(event);
        new PublishPendingOutboxEventsService(repository, broker, 10, 3, 60).publishBatch();
        verify(repository).scheduleRetry(eq(event.eventId()), eq(1), any(), eq("Kafka unavailable"));
    }

    @Test void marksEventFailedAfterMaximumAttempts() throws Exception {
        OutboxEvent event = event(2);
        when(repository.claimPendingBatch(anyInt(), any(), any(), anyString())).thenReturn(List.of(event));
        doThrow(new RuntimeException("Kafka unavailable")).when(broker).publish(event);
        new PublishPendingOutboxEventsService(repository, broker, 10, 3, 60).publishBatch();
        verify(repository).markFailed(event.eventId(), 3, "Kafka unavailable");
    }

    private OutboxEvent event(int retries) {
        var now = LocalDateTime.now();
        return new OutboxEvent(1L, UUID.randomUUID(), OutboxEvent.EventType.COURSE_CREATED, "9", "COURSE", "{}",
                now, OutboxEvent.Status.PROCESSING, retries, now, null, now, "worker", null);
    }
}
