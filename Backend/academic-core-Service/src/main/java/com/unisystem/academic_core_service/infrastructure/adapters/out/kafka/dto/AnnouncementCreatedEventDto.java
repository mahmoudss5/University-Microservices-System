package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.dto;

import java.time.LocalDateTime;

public record AnnouncementCreatedEventDto(
        String id,
        String courseId,
        String courseName,
        String title,
        String description,
        LocalDateTime createdAt) {
}
