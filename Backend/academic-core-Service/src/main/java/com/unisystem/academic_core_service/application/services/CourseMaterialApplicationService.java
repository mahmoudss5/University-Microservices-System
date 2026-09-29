package com.unisystem.academic_core_service.application.services;

import com.unisystem.academic_core_service.application.port.in.CourseMaterialUseCase;
import com.unisystem.academic_core_service.application.port.out.CourseMaterialRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.ObjectStoragePort;
import com.unisystem.academic_core_service.domain.exceptions.CourseNotFoundException;
import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.domain.model.CourseMaterial;
import com.unisystem.academic_core_service.domain.model.MaterialStatus;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class CourseMaterialApplicationService implements CourseMaterialUseCase {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final CourseMaterialRepositoryPort materialRepository;
    private final ObjectStoragePort objectStorage;
    private final CourseRepositoryPort courseRepository;

    public CourseMaterialApplicationService(
            CourseMaterialRepositoryPort materialRepository,
            ObjectStoragePort objectStorage,
            CourseRepositoryPort courseRepository
    ) {
        this.materialRepository = materialRepository;
        this.objectStorage = objectStorage;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public UploadResponse initiateUpload(InitUploadCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException(command.courseId()));

        validate(command);

        String objectKey = String.format(
                "courses/%d/materials/%s/%s",
                course.getId(),
                UUID.randomUUID(),
                sanitizeFilename(command.originalFilename())
        );

        CourseMaterial material = CourseMaterial.pending(
                course.getId(),
                command.title(),
                command.originalFilename(),
                objectKey,
                command.contentType(),
                command.fileSize()
        );

        CourseMaterial saved = materialRepository.save(material);

        ObjectStoragePort.PresignedUpload upload = objectStorage.createUploadUrl(
                new ObjectStoragePort.UploadRequest(
                        objectKey,
                        command.contentType(),
                        Duration.ofMinutes(10)
                )
        );

        return new UploadResponse(
                saved.getId(),
                upload.objectKey(),
                upload.url(),
                upload.expiresAt()
        );
    }

    @Override
    @Transactional
    public CourseMaterial completeUpload(Long courseId, Long materialId) {
        CourseMaterial material = getMaterial(courseId, materialId);

        ObjectStoragePort.StoredObjectMetadata metadata =
                objectStorage.headObject(material.getS3Key());

        if (!Objects.equals(material.getMaterialType(), metadata.contentType())) {
            throw new IllegalArgumentException("Uploaded content type does not match");
        }

        material.markUploaded(metadata.size());
        return materialRepository.save(material);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseMaterial> findByCourseId(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        return materialRepository.findByCourseId(courseId)
                .stream()
                .filter(material -> material.getStatus() == MaterialStatus.UPLOADED)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public URI createDownloadUrl(Long courseId, Long materialId) {
        CourseMaterial material = getMaterial(courseId, materialId);

        if (material.getStatus() != MaterialStatus.UPLOADED) {
            throw new IllegalStateException("Material is not uploaded yet");
        }

        return objectStorage.createDownloadUrl(
                material.getS3Key(),
                Duration.ofMinutes(5)
        );
    }

    @Override
    @Transactional
    public void delete(Long courseId, Long materialId) {
        CourseMaterial material = getMaterial(courseId, materialId);
        objectStorage.deleteObject(material.getS3Key());
        materialRepository.deleteById(material.getId());
    }

    private CourseMaterial getMaterial(Long courseId, Long materialId) {
        CourseMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found"));

        if (!Objects.equals(material.getCourseId(), courseId)) {
            throw new IllegalArgumentException("Material does not belong to this course");
        }

        return material;
    }

    private void validate(InitUploadCommand command) {
        if (command.fileSize() == null || command.fileSize() <= 0) {
            throw new IllegalArgumentException("File size must be greater than zero");
        }

        if (command.fileSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Maximum file size is 50 MB");
        }

        if (command.contentType() == null || !ALLOWED_CONTENT_TYPES.contains(command.contentType())) {
            throw new IllegalArgumentException("File type is not allowed");
        }
    }

    private String sanitizeFilename(String filename) {
        return filename
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_+", "_");
    }
}
