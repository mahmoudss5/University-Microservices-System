package com.unisystem.academic_core_service.application.port.in;

import com.unisystem.academic_core_service.domain.model.Enrollment;
import java.math.BigDecimal;

public interface EnrollStudentUseCase {
    Enrollment enroll(EnrollCommand cmd);
    void drop(Long studentId, Long courseId);
    Enrollment complete(Long studentId, Long courseId, BigDecimal grade, boolean passed);

    record EnrollCommand(Long studentId, Long courseId) {}
}
