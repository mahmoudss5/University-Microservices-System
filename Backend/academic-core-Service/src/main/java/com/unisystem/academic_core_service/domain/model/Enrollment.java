package com.unisystem.academic_core_service.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
public class Enrollment {

    private Long id;
    private Long studentId;
    private Long courseId;
    private LocalDateTime enrolledAt;
    private EnrollmentStatus status;
    private BigDecimal grade;
    private Boolean passed;


    public static Enrollment create(Long studentId, Long courseId) {
        Enrollment e = new Enrollment();
        e.studentId = studentId;
        e.courseId = courseId;
        e.enrolledAt = LocalDateTime.now();
        e.status = EnrollmentStatus.ENROLLED;
        return e;
    }

    public void complete(BigDecimal grade, boolean passed) {
        if (status != EnrollmentStatus.ENROLLED) throw new IllegalStateException("Only an active enrollment can be completed");
        this.grade = grade;
        this.passed = passed;
        this.status = passed ? EnrollmentStatus.COMPLETED : EnrollmentStatus.FAILED;
    }

    public void drop() { this.status = EnrollmentStatus.DROPPED; }
    public void reactivate() {
        this.status = EnrollmentStatus.ENROLLED;
        this.grade = null;
        this.passed = null;
        this.enrolledAt = LocalDateTime.now();
    }


}
