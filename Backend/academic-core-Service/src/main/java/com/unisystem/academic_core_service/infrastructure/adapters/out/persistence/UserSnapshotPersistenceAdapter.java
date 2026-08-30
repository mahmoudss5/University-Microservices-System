package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence;

import com.unisystem.academic_core_service.application.port.out.UserSnapshotRepositoryPort;
import com.unisystem.academic_core_service.domain.model.UserSnapshot;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.UserSnapshotEntity;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository.UserSnapshotJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSnapshotPersistenceAdapter implements UserSnapshotRepositoryPort {
    private final UserSnapshotJpaRepository repository;

    @Override public UserSnapshot save(UserSnapshot snapshot) { return toDomain(repository.save(toEntity(snapshot))); }
    @Override public Optional<UserSnapshot> findById(Long userId) { return repository.findById(userId).map(this::toDomain); }
    @Override public void deleteById(Long userId) { repository.deleteById(userId); }

    private UserSnapshotEntity toEntity(UserSnapshot snapshot) {
        return UserSnapshotEntity.builder().userId(snapshot.userId()).userName(snapshot.userName())
                .role(snapshot.role()).active(snapshot.active()).updatedAt(snapshot.updatedAt()).build();
    }
    private UserSnapshot toDomain(UserSnapshotEntity entity) {
        return new UserSnapshot(entity.getUserId(), entity.getUserName(), entity.getRole(), entity.isActive(), entity.getUpdatedAt());
    }
}
