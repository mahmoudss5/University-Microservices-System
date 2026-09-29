package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.mapper;

import com.unisystem.academic_core_service.domain.model.CourseMaterial;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.CourseMaterialEntity;
import org.springframework.stereotype.Component;

@Component
public class CourseMaterialPersistenceMapper {

    public CourseMaterialEntity toEntity(CourseMaterial material) {
        return CourseMaterialEntity.builder()
                .id(material.getId())
                .courseId(material.getCourseId())
                .title(material.getTitle())
                .originalFilename(material.getOriginalFilename())
                .objectKey(material.getS3Key())
                .contentType(material.getMaterialType())
                .fileSize(material.getFileSize())
                .status(material.getStatus())
                .createdAt(material.getCreatedAt())
                .uploadedAt(material.getUploadedAt())
                .build();
    }

    public CourseMaterial toDomain(CourseMaterialEntity entity) {
        CourseMaterial material = new CourseMaterial();

        material.setId(entity.getId());
        material.setCourseId(entity.getCourseId());
        material.setTitle(entity.getTitle());
        material.setOriginalFilename(entity.getOriginalFilename());
        material.setS3Key(entity.getObjectKey());
        material.setMaterialType(entity.getContentType());
        material.setFileSize(entity.getFileSize());
        material.setStatus(entity.getStatus());
        material.setCreatedAt(entity.getCreatedAt());
        material.setUploadedAt(entity.getUploadedAt());


        return material;
    }
}
