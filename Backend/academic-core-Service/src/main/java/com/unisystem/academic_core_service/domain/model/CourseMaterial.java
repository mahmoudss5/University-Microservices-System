package com.unisystem.academic_core_service.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CourseMaterial {

    private Long id;
    private Long courseId;
    private String title;
    private String originalFilename;
    private String s3Key;
    private String materialType;
    private Long fileSize;
    private MaterialStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime uploadedAt;

    public static CourseMaterial pending(
            Long courseId,
            String title,
            String originalFilename,
            String s3Key,
            String materialType,
            Long fileSize
    ) {
        CourseMaterial material = new CourseMaterial();
        material.courseId = courseId;
        material.title = title;
        material.originalFilename = originalFilename;
        material.s3Key = s3Key;
        material.materialType = materialType;
        material.fileSize = fileSize;
        material.status = MaterialStatus.PENDING;
        material.createdAt = LocalDateTime.now();
        return material;
    }

    public void markUploaded(long actualFileSize) {
        if (status != MaterialStatus.PENDING) {
            throw new IllegalStateException("Course material is not pending");
        }
        if (fileSize == null || fileSize != actualFileSize) {
            throw new IllegalArgumentException("Course material file size does not match");
        }

        this.fileSize = actualFileSize;
        this.status = MaterialStatus.UPLOADED;
        this.uploadedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = MaterialStatus.FAILED;
    }
}
