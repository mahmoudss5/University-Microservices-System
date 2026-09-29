package com.unisystem.academic_core_service.infrastructure.adapters.in.http.services;

import com.unisystem.academic_core_service.application.port.in.CourseMaterialUseCase;
import com.unisystem.academic_core_service.domain.model.CourseMaterial;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Request.CreateMaterialUploadRequest;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.CourseMaterialResponse;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.UploadUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseMaterialHttpService {

    private final CourseMaterialUseCase courseMaterialUseCase;

    public UploadUrlResponse initiateUpload(
            Long courseId,
            CreateMaterialUploadRequest request
    ) {
        CourseMaterialUseCase.UploadResponse response =
                courseMaterialUseCase.initiateUpload(
                        new CourseMaterialUseCase.InitUploadCommand(
                                courseId,
                                request.title(),
                                request.originalFilename(),
                                request.contentType(),
                                request.fileSize()
                        )
                );

        return new UploadUrlResponse(
                response.materialId(),
                response.objectKey(),
                response.uploadUrl(),
                response.expiresAt()
        );
    }

    public CourseMaterialResponse completeUpload(Long courseId, Long materialId) {
        return toResponse(courseMaterialUseCase.completeUpload(courseId, materialId));
    }

    public List<CourseMaterialResponse> findByCourseId(Long courseId) {
        return courseMaterialUseCase.findByCourseId(courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public URI createDownloadUrl(Long courseId, Long materialId) {
        return courseMaterialUseCase.createDownloadUrl(courseId, materialId);
    }

    public void delete(Long courseId, Long materialId) {
        courseMaterialUseCase.delete(courseId, materialId);
    }

    private CourseMaterialResponse toResponse(CourseMaterial material) {
        return new CourseMaterialResponse(
                material.getId(),
                material.getCourseId(),
                material.getTitle(),
                material.getOriginalFilename(),
                material.getMaterialType(),
                material.getFileSize(),
                material.getStatus(),
                material.getCreatedAt(),
                material.getUploadedAt()
        );
    }
}
