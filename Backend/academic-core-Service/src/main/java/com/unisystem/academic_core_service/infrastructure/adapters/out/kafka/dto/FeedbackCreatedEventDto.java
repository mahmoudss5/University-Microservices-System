package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.dto;

import java.time.LocalDateTime;

public record FeedbackCreatedEventDto(String feedbackId, String studentId, String courseId, String comment, LocalDateTime createdAt) {
}
