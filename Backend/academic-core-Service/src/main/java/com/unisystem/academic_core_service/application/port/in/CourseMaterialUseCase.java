package com.unisystem.academic_core_service.application.port.in;

import com.unisystem.academic_core_service.domain.model.CourseMaterial;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public interface CourseMaterialUseCase {
    UploadResponse initiateUpload(InitUploadCommand command);

    CourseMaterial completeUpload(Long courseId, Long materialId);

    List<CourseMaterial> findByCourseId(Long courseId);

    URI createDownloadUrl(Long courseId, Long materialId);

    void delete(Long courseId, Long materialId);

    record InitUploadCommand(
            Long courseId,
            String title,
            String originalFilename,
            String contentType,
            Long fileSize
    ) {
    }

    record UploadResponse(
            Long materialId,
            String objectKey,
            URI uploadUrl,
            Instant expiresAt
    ) {
    }
}
