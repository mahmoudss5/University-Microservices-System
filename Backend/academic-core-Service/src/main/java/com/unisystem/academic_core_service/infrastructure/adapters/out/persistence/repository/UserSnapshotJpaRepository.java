package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository;

import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.UserSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSnapshotJpaRepository extends JpaRepository<UserSnapshotEntity, Long> {}
