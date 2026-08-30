package com.unisystem.academic_core_service.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.unisystem.academic_core_service.application.port.out.CoursePrerequisiteRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.domain.exceptions.InvalidPrerequisiteException;
import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.domain.model.CoursePrerequisite;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ManageCoursePrerequisitesServiceTest {
    private final CourseRepositoryPort courses = mock(CourseRepositoryPort.class);
    private final CoursePrerequisiteRepositoryPort repository = mock(CoursePrerequisiteRepositoryPort.class);
    private final ManageCoursePrerequisitesService service = new ManageCoursePrerequisitesService(courses, repository);

    @Test void addsValidRelationship() {
        exists(1L); exists(2L);
        when(repository.findByCourseId(2L)).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(new CoursePrerequisite(1L, 2L), service.add(1L, 2L));
    }

    @Test void rejectsIndirectCycle() {
        exists(1L); exists(2L);
        when(repository.findByCourseId(2L)).thenReturn(List.of(new CoursePrerequisite(2L, 3L)));
        when(repository.findByCourseId(3L)).thenReturn(List.of(new CoursePrerequisite(3L, 1L)));
        assertThrows(InvalidPrerequisiteException.class, () -> service.add(1L, 2L));
        verify(repository, never()).save(any());
    }

    @Test void rejectsSelfReference() {
        assertThrows(IllegalArgumentException.class, () -> service.add(1L, 1L));
    }

    private void exists(Long id) { Course course = new Course(); course.setId(id); when(courses.findById(id)).thenReturn(Optional.of(course)); }
}
