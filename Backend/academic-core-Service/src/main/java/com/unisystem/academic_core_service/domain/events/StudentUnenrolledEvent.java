package com.unisystem.academic_core_service.domain.events;

public record StudentUnenrolledEvent(String studentId, String courseId) {
}
