package com.unisystem.academic_core_service.application.services;

import com.unisystem.academic_core_service.application.port.in.SynchronizeUserSnapshotUseCase;
import com.unisystem.academic_core_service.application.port.out.UserSnapshotRepositoryPort;
import com.unisystem.academic_core_service.domain.model.UserSnapshot;
import java.time.LocalDateTime;

public class SynchronizeUserSnapshotService implements SynchronizeUserSnapshotUseCase {
    private final UserSnapshotRepositoryPort repository;
    public SynchronizeUserSnapshotService(UserSnapshotRepositoryPort repository) { this.repository = repository; }

    @Override
    public void synchronize(Command command) {
        if (command.userId() == null) throw new IllegalArgumentException("User id is required");
        if (command.deleted()) {
            repository.deleteById(command.userId());
            return;
        }
        repository.save(new UserSnapshot(command.userId(), command.userName(), command.role(), command.active(), LocalDateTime.now()));
    }
}
