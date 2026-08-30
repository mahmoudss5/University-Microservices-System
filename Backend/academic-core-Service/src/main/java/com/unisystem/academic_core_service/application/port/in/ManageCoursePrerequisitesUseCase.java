package com.unisystem.academic_core_service.application.port.in;

import com.unisystem.academic_core_service.domain.model.CoursePrerequisite;

public interface ManageCoursePrerequisitesUseCase {
    CoursePrerequisite add(Long courseId, Long prerequisiteCourseId);
    void remove(Long courseId, Long prerequisiteCourseId);
}
