package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.kafkaAdapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unisystem.academic_core_service.application.port.out.MessageBrokerPort;
import com.unisystem.academic_core_service.domain.model.OutboxEvent;
import com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.kafkaConifg.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements MessageBrokerPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(OutboxEvent event) throws Exception {
        String topic = switch (event.eventType()) {
            case STUDENT_ENROLLED -> KafkaTopics.STUDENT_ENROLLED;
            case STUDENT_UNENROLLED -> KafkaTopics.STUDENT_UNENROLLED;
            case COURSE_CREATED -> KafkaTopics.COURSE_CREATED;
            case COURSE_DELETED -> KafkaTopics.COURSE_DELETED;
            case ANNOUNCEMENT_CREATED -> KafkaTopics.ANNOUNCEMENT_CREATED;
            case FEEDBACK_CREATED -> KafkaTopics.FEEDBACK_CREATED;
        };
        var result = kafkaTemplate.send(topic, event.aggregateId(), objectMapper.readTree(event.eventData())).get();
        log.info("Published outbox event eventId={} type={} topic={} partition={} offset={}", event.eventId(),
                event.eventType(), topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
    }
}
