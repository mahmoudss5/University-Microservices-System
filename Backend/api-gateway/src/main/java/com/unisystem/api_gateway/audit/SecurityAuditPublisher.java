package com.unisystem.api_gateway.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditPublisher {
    private static final String TOPIC = "security-audit-events.v1";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(SecurityAuditEvent event) {
        kafkaTemplate.send(TOPIC, event.aggregateId(), event).whenComplete((result, error) -> {
            if (error != null) log.error("Failed to publish security audit event {}", event.eventId(), error);
        });
    }
}
