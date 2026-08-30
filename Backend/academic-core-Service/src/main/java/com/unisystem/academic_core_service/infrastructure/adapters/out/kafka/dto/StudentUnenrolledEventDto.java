package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.dto;

public record StudentUnenrolledEventDto(String studentId, String courseId) {
}
