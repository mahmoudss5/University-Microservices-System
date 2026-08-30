package com.unisystem.academic_core_service.domain.events;

import java.time.LocalDateTime;

public record FeedbackCreatedEvent(String feedbackId, String studentId, String courseId, String comment, LocalDateTime createdAt) {
}
