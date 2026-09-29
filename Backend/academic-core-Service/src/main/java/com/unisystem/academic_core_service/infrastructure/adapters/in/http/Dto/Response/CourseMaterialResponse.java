package com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response;

import com.unisystem.academic_core_service.domain.model.MaterialStatus;

import java.time.LocalDateTime;

public record CourseMaterialResponse(
        Long id,
        Long courseId,
        String title,
        String originalFilename,
        String materialType,
        Long fileSize,
        MaterialStatus status,
        LocalDateTime createdAt,
        LocalDateTime uploadedAt
) {
}
