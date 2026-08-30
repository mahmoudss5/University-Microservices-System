package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence;

import com.unisystem.academic_core_service.application.port.out.CoursePrerequisiteRepositoryPort;
import com.unisystem.academic_core_service.domain.model.CoursePrerequisite;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.CoursePrerequisiteEntity;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.CoursePrerequisiteId;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository.CoursePrerequisiteJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoursePrerequisitePersistenceAdapter implements CoursePrerequisiteRepositoryPort {
    private final CoursePrerequisiteJpaRepository repository;
    @Override public CoursePrerequisite save(CoursePrerequisite value) {
        CoursePrerequisiteId id = new CoursePrerequisiteId(value.courseId(), value.prerequisiteCourseId());
        repository.save(new CoursePrerequisiteEntity(id));
        return value;
    }
    @Override public boolean exists(Long courseId, Long prerequisiteCourseId) {
        return repository.existsById(new CoursePrerequisiteId(courseId, prerequisiteCourseId));
    }
    @Override public List<CoursePrerequisite> findByCourseId(Long courseId) {
        return repository.findByIdCourseId(courseId).stream()
                .map(entity -> new CoursePrerequisite(entity.getId().getCourseId(), entity.getId().getPrerequisiteCourseId())).toList();
    }
    @Override public void delete(Long courseId, Long prerequisiteCourseId) {
        repository.deleteById(new CoursePrerequisiteId(courseId, prerequisiteCourseId));
    }
    @Override public boolean isRequiredByAnyCourse(Long prerequisiteCourseId) {
        return repository.existsByIdPrerequisiteCourseId(prerequisiteCourseId);
    }
}
