package com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response;

import java.net.URI;
import java.time.Instant;

public record UploadUrlResponse(
        Long materialId,
        String objectKey,
        URI uploadUrl,
        Instant expiresAt
) {
}
