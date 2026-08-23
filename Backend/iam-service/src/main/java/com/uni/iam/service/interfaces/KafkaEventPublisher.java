package com.uni.iam.service.interfaces;

import com.uni.iam.entity.User;

public interface KafkaEventPublisher {
    void publishUserRegisteredEvent(User user);
}
