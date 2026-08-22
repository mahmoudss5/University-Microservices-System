package com.unisystem.academic_core_service.domain.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.unisystem.academic_core_service.domain.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.domain.application.port.out.EnrollmentRepositoryPort;
import com.unisystem.academic_core_service.domain.exceptions.CourseNotFoundException;
import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.domain.model.Enrollment;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class QueryServicesTest {
    private final CourseRepositoryPort courses = mock(CourseRepositoryPort.class);
    private final EnrollmentRepositoryPort enrollments = mock(EnrollmentRepositoryPort.class);

    @Test
    void courseQueriesDelegateAndReturnRepositoryResults() {
        GetCoursesService service = new GetCoursesService(courses);
        Course course = new Course();
        course.setId(4L);
        when(courses.findAll()).thenReturn(List.of(course));
        when(courses.findById(4L)).thenReturn(Optional.of(course));
        when(courses.findByIds(List.of(4L))).thenReturn(List.of(course));
        when(courses.findByTeacherName("Ada")).thenReturn(List.of(course));
        when(courses.findByTeacherId(8L)).thenReturn(List.of(course));
        when(courses.findByCourseName("Algorithms")).thenReturn(Optional.of(course));
        when(courses.findByDepartmentName("CS")).thenReturn(List.of(course));
        when(courses.findPopular(3)).thenReturn(List.of(course));

        assertSame(course, service.findAll().getFirst());
        assertSame(course, service.findById(4L).orElseThrow());
        assertSame(course, service.findByIds(List.of(4L)).getFirst());
        assertSame(course, service.findByTeacherName("Ada").getFirst());
        assertSame(course, service.findByTeacherId(8L).getFirst());
        assertSame(course, service.findByCourseName("Algorithms").orElseThrow());
        assertSame(course, service.findByDepartmentName("CS").getFirst());
        assertSame(course, service.findPopular(3).getFirst());
    }

    @Test
    void emptyCourseIdsDoNotCallRepository() {
        GetCoursesService service = new GetCoursesService(courses);

        assertTrue(service.findByIds(null).isEmpty());
        assertTrue(service.findByIds(List.of()).isEmpty());

        verify(courses, never()).findByIds(any());
    }

    @Test
    void missingCourseThrowsDomainException() {
        GetCoursesService service = new GetCoursesService(courses);
        when(courses.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void enrollmentQueriesDelegateAndPreserveOptional() {
        GetEnrollmentsService service = new GetEnrollmentsService(enrollments);
        Enrollment enrollment = new Enrollment();
        when(enrollments.findByStudentId(2L)).thenReturn(List.of(enrollment));
        when(enrollments.findByCourseId(3L)).thenReturn(List.of(enrollment));
        when(enrollments.findByStudentIdAndCourseId(2L, 3L)).thenReturn(Optional.of(enrollment));
        when(enrollments.findAll()).thenReturn(List.of(enrollment));

        assertSame(enrollment, service.getEnrollmentsByStudentId(2L).getFirst());
        assertSame(enrollment, service.getEnrollmentsByCourseId(3L).getFirst());
        assertSame(enrollment, service.getEnrollment(2L, 3L).orElseThrow());
        assertSame(enrollment, service.getAllEnrollments().getFirst());
    }
}
