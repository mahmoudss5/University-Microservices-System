package com.unisystem.academic_core_service.domain.model;

public record CoursePrerequisite(Long courseId, Long prerequisiteCourseId) {
    public CoursePrerequisite {
        if (courseId == null || prerequisiteCourseId == null) {
            throw new IllegalArgumentException("Course and prerequisite ids are required");
        }
        if (courseId.equals(prerequisiteCourseId)) {
            throw new IllegalArgumentException("A course cannot be its own prerequisite");
        }
    }
}
