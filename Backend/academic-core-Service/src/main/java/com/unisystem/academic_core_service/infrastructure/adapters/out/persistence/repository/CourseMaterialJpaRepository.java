package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository;

import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.CourseMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseMaterialJpaRepository extends JpaRepository<CourseMaterialEntity, Long> {
    List<CourseMaterialEntity> findByCourseIdOrderByCreatedAtDesc(Long courseId);
}
