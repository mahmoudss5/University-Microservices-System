package com.unisystem.academic_core_service.application.port.out;

import com.unisystem.academic_core_service.domain.model.CoursePrerequisite;
import java.util.List;

public interface CoursePrerequisiteRepositoryPort {
    CoursePrerequisite save(CoursePrerequisite prerequisite);
    boolean exists(Long courseId, Long prerequisiteCourseId);
    List<CoursePrerequisite> findByCourseId(Long courseId);
    void delete(Long courseId, Long prerequisiteCourseId);
    boolean isRequiredByAnyCourse(Long prerequisiteCourseId);
}
