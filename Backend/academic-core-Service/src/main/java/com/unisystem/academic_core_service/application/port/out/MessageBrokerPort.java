package com.unisystem.academic_core_service.application.port.out;

import com.unisystem.academic_core_service.domain.model.OutboxEvent;

public interface MessageBrokerPort {
    void publish(OutboxEvent event) throws Exception;
}
