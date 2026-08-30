package com.unisystem.academic_core_service.application.port.in;

import com.unisystem.academic_core_service.domain.model.UserRole;

public interface SynchronizeUserSnapshotUseCase {
    void synchronize(Command command);
    record Command(Long userId, String userName, UserRole role, boolean active, boolean deleted) {}
}
