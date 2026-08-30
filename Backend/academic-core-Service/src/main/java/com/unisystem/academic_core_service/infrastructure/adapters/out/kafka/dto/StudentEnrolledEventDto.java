package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.dto;

public record StudentEnrolledEventDto(String studentId, String enrolledCourseId, String courseName) {
}
