package com.unisystem.academic_core_service.application.port.in;

import com.unisystem.academic_core_service.domain.model.CoursePrerequisite;
import java.util.List;

public interface GetCoursePrerequisitesQuery {
    List<CoursePrerequisite> findByCourseId(Long courseId);
}
