package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.dto;

import java.time.LocalDate;

public record CourseCreatedEventDto(
        String courseId, String courseName, String courseCode, LocalDate createdAt) {
}
