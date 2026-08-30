package com.unisystem.academic_core_service.infrastructure.adapters.in.scheduling;

import com.unisystem.academic_core_service.application.port.in.PublishPendingOutboxEventsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {
    private final PublishPendingOutboxEventsUseCase relay;

    @Scheduled(fixedDelayString = "${outbox.relay.delay-ms:1000}")
    public void publishPendingEvents() { relay.publishBatch(); }
}
