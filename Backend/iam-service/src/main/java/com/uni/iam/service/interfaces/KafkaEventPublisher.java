package com.uni.iam.service.interfaces;

import com.uni.iam.entity.User;

public interface KafkaEventPublisher {
    void publishUserRegisteredEvent(User user);
    void publishUserUpdatedEvent(User user);
    void publishUserDeactivatedEvent(User user);
    void publishUserDeletedEvent(User user);
}
