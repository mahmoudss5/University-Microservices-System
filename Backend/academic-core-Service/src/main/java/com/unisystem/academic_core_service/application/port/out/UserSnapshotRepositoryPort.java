package com.unisystem.academic_core_service.application.port.out;

import com.unisystem.academic_core_service.domain.model.UserSnapshot;
import java.util.Optional;

public interface UserSnapshotRepositoryPort {
    UserSnapshot save(UserSnapshot snapshot);
    Optional<UserSnapshot> findById(Long userId);
    void deleteById(Long userId);
}
