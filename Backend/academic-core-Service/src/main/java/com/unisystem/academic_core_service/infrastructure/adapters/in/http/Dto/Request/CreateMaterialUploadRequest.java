package com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateMaterialUploadRequest(
        @NotBlank String title,
        @NotBlank String originalFilename,
        @NotBlank String contentType,
        @NotNull @Positive Long fileSize
) {
}
