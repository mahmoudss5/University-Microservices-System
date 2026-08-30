package com.unisystem.academic_core_service.application.services;

import com.unisystem.academic_core_service.application.port.in.GetCoursePrerequisitesQuery;
import com.unisystem.academic_core_service.application.port.in.ManageCoursePrerequisitesUseCase;
import com.unisystem.academic_core_service.application.port.out.CoursePrerequisiteRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.domain.exceptions.CourseNotFoundException;
import com.unisystem.academic_core_service.domain.exceptions.InvalidPrerequisiteException;
import com.unisystem.academic_core_service.domain.model.CoursePrerequisite;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManageCoursePrerequisitesService implements ManageCoursePrerequisitesUseCase, GetCoursePrerequisitesQuery {
    private final CourseRepositoryPort courses;
    private final CoursePrerequisiteRepositoryPort prerequisites;

    public ManageCoursePrerequisitesService(CourseRepositoryPort courses, CoursePrerequisiteRepositoryPort prerequisites) {
        this.courses = courses;
        this.prerequisites = prerequisites;
    }

    @Override
    public CoursePrerequisite add(Long courseId, Long prerequisiteCourseId) {
        CoursePrerequisite relationship = new CoursePrerequisite(courseId, prerequisiteCourseId);
        requireCourse(courseId);
        requireCourse(prerequisiteCourseId);
        if (prerequisites.exists(courseId, prerequisiteCourseId)) {
            throw new InvalidPrerequisiteException("Prerequisite relationship already exists");
        }
        if (reaches(prerequisiteCourseId, courseId, new HashSet<>())) {
            throw new InvalidPrerequisiteException("Prerequisite relationship would create a cycle");
        }
        return prerequisites.save(relationship);
    }

    @Override public void remove(Long courseId, Long prerequisiteCourseId) { prerequisites.delete(courseId, prerequisiteCourseId); }
    @Override public List<CoursePrerequisite> findByCourseId(Long courseId) { requireCourse(courseId); return prerequisites.findByCourseId(courseId); }

    private boolean reaches(Long from, Long target, Set<Long> visited) {
        if (from.equals(target)) return true;
        if (!visited.add(from)) return false;
        return prerequisites.findByCourseId(from).stream()
                .anyMatch(edge -> reaches(edge.prerequisiteCourseId(), target, visited));
    }

    private void requireCourse(Long id) { courses.findById(id).orElseThrow(() -> new CourseNotFoundException(id)); }
}
