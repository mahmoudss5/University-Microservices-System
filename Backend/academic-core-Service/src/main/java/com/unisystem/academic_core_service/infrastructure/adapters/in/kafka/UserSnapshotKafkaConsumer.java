package com.unisystem.academic_core_service.infrastructure.adapters.in.kafka;

import com.unisystem.academic_core_service.application.port.in.SynchronizeUserSnapshotUseCase;
import com.unisystem.academic_core_service.infrastructure.adapters.in.kafka.dto.UserSnapshotEventDto;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSnapshotKafkaConsumer {
    private final SynchronizeUserSnapshotUseCase synchronizeUserSnapshot;

    @KafkaListener(topics = {"user-registered-v1", "user-updated-v1", "user-deactivated-v1", "user-deleted-v1", "student-registered"})
    public void consume(Map<String, Object> payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        UserSnapshotEventDto dto = UserSnapshotEventDto.from(payload);
        synchronizeUserSnapshot.synchronize(UserSnapshotEventMapper.toCommand(dto, "user-deleted-v1".equals(topic)));
    }
}
