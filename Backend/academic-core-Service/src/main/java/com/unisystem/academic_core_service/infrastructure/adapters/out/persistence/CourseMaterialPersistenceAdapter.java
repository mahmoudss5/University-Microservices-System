package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence;

import com.unisystem.academic_core_service.application.port.out.CourseMaterialRepositoryPort;
import com.unisystem.academic_core_service.domain.model.CourseMaterial;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.CourseMaterialEntity;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.mapper.CourseMaterialPersistenceMapper;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository.CourseMaterialJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CourseMaterialPersistenceAdapter  implements CourseMaterialRepositoryPort {
    private final CourseMaterialJpaRepository repository;
    private final CourseMaterialPersistenceMapper mapper;


    @Override
    public CourseMaterial save(CourseMaterial material) {
        CourseMaterialEntity entity = repository.save(mapper.toEntity(material));
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<CourseMaterial> findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<CourseMaterial> findByCourseId(Long courseId) {
        return repository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
