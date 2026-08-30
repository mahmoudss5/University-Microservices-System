package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository;

import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.CoursePrerequisiteEntity;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.CoursePrerequisiteId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePrerequisiteJpaRepository extends JpaRepository<CoursePrerequisiteEntity, CoursePrerequisiteId> {
    List<CoursePrerequisiteEntity> findByIdCourseId(Long courseId);
    boolean existsByIdPrerequisiteCourseId(Long prerequisiteCourseId);
}
