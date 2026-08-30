package com.unisystem.academic_core_service.infrastructure.adapters.in.kafka;

import com.unisystem.academic_core_service.application.port.in.SynchronizeUserSnapshotUseCase;
import com.unisystem.academic_core_service.domain.model.UserRole;
import com.unisystem.academic_core_service.infrastructure.adapters.in.kafka.dto.UserSnapshotEventDto;

public final class UserSnapshotEventMapper {
    private UserSnapshotEventMapper() {}
    public static SynchronizeUserSnapshotUseCase.Command toCommand(UserSnapshotEventDto dto, boolean deleted) {
        return new SynchronizeUserSnapshotUseCase.Command(dto.userId(), dto.username(),
                UserRole.valueOf(dto.role().toUpperCase()), Boolean.TRUE.equals(dto.active()), deleted);
    }
}
