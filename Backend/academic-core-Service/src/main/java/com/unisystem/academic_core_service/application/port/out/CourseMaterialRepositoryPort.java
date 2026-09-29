package com.unisystem.academic_core_service.application.port.out;

import com.unisystem.academic_core_service.domain.model.CourseMaterial;

import java.util.List;
import java.util.Optional;

public interface CourseMaterialRepositoryPort {

    CourseMaterial save(CourseMaterial courseMaterial);
    Optional<CourseMaterial> findById(Long id);

    List<CourseMaterial> findByCourseId(Long courseId);

    void deleteById(Long id);
}
