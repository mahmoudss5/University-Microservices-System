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
        Map<String, Object> event = new HashMap<>();
        event.put("userId", user.getId());
        event.put("username", user.getUsername());
        event.put("email", user.getEmail());
        event.put("role", user.getRole().name());
        
        kafkaTemplate.send("student-registered", String.valueOf(user.getId()), event);
        log.info("Published user registered event for userId: {}", user.getId());
    }
}
