package com.uni.iam.service.impl;

import com.uni.iam.entity.User;
import com.uni.iam.service.interfaces.KafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventPublisherImpl implements KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishUserRegisteredEvent(User user) {
        publish("user-registered-v1", user);
        // Temporary compatibility for existing consumers; remove after migration.
        publish("student-registered", user);
    }

    @Override public void publishUserUpdatedEvent(User user) { publish("user-updated-v1", user); }
    @Override public void publishUserDeactivatedEvent(User user) { publish("user-deactivated-v1", user); }
    @Override public void publishUserDeletedEvent(User user) { publish("user-deleted-v1", user); }

    private void publish(String topic, User user) {
        Map<String, Object> event = new HashMap<>();
        event.put("userId", user.getId());
        event.put("username", user.getUsername());
        event.put("email", user.getEmail());
        event.put("role", user.getRole().name());
        event.put("active", user.isActive());
        
        kafkaTemplate.send(topic, String.valueOf(user.getId()), event);
        log.info("Published {} event for userId: {}", topic, user.getId());
    }
}
