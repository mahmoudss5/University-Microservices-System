package com.unisystem.academic_core_service.domain.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.unisystem.academic_core_service.domain.application.port.in.EnrollStudentUseCase.EnrollCommand;
import com.unisystem.academic_core_service.domain.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.domain.application.port.out.EnrollmentRepositoryPort;
import com.unisystem.academic_core_service.domain.application.port.out.EventPublisherPort;
import com.unisystem.academic_core_service.domain.events.StudentEnrollend;
import com.unisystem.academic_core_service.domain.exceptions.AlreadyEnrolledException;
import com.unisystem.academic_core_service.domain.exceptions.CourseNotFoundException;
import com.unisystem.academic_core_service.domain.exceptions.InvalidEnrollmentException;
import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.domain.model.Enrollment;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EnrollStudentServiceTest {
    private final CourseRepositoryPort courses = mock(CourseRepositoryPort.class);
    private final EnrollmentRepositoryPort enrollments = mock(EnrollmentRepositoryPort.class);
    private final EventPublisherPort publisher = mock(EventPublisherPort.class);
    private final EnrollStudentService service = new EnrollStudentService(courses, enrollments, publisher);

    @Test
    void enrollsStudentUpdatesCapacityAndPublishesEvent() {
        Course course = course(7L, "Networks", 2, 10);
        when(courses.findByIdWithLock(7L)).thenReturn(Optional.of(course));
        when(enrollments.findByStudentIdAndCourseId(3L, 7L)).thenReturn(Optional.empty());

        Enrollment result = service.enroll(new EnrollCommand(3L, 7L));

        assertAll(
                () -> assertEquals(3L, result.getStudentId()),
                () -> assertEquals(7L, result.getCourseId()),
                () -> assertNotNull(result.getEnrolledAt()),
                () -> assertEquals(3, course.getEnrolledCount()));
        verify(courses).save(course);
        verify(enrollments).save(result);
        ArgumentCaptor<StudentEnrollend> event = ArgumentCaptor.forClass(StudentEnrollend.class);
        verify(publisher).publishStudentEnrolled(event.capture());
        assertEquals("Networks", event.getValue().courseName());
    }

    @Test
    void rejectsDuplicateEnrollmentWithoutChangingCourse() {
        Course course = course(7L, "Networks", 2, 10);
        when(courses.findByIdWithLock(7L)).thenReturn(Optional.of(course));
        when(enrollments.findByStudentIdAndCourseId(3L, 7L)).thenReturn(Optional.of(new Enrollment()));

        assertThrows(AlreadyEnrolledException.class, () -> service.enroll(new EnrollCommand(3L, 7L)));

        assertEquals(2, course.getEnrolledCount());
        verify(courses, never()).save(any());
        verify(enrollments, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void rejectsEnrollmentWhenCourseDoesNotExist() {
        when(courses.findByIdWithLock(7L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () -> service.enroll(new EnrollCommand(3L, 7L)));

        verifyNoInteractions(enrollments, publisher);
    }

    @Test
    void dropsExistingEnrollmentAndDecrementsCount() {
        Course course = course(7L, "Networks", 2, 10);
        Enrollment enrollment = new Enrollment();
        enrollment.setId(21L);
        when(courses.findById(7L)).thenReturn(Optional.of(course));
        when(enrollments.findByStudentIdAndCourseId(3L, 7L)).thenReturn(Optional.of(enrollment));

        service.drop(3L, 7L);

        verify(enrollments).deleteById(21L);
        assertEquals(1, course.getEnrolledCount());
    }

    @Test
    void rejectsDropWhenEnrollmentDoesNotExist() {
        when(courses.findById(7L)).thenReturn(Optional.of(course(7L, "Networks", 2, 10)));
        when(enrollments.findByStudentIdAndCourseId(3L, 7L)).thenReturn(Optional.empty());

        assertThrows(InvalidEnrollmentException.class, () -> service.drop(3L, 7L));

        verify(enrollments, never()).deleteById(anyLong());
    }

    private Course course(long id, String name, int enrolled, int maximum) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setEnrolledCount(enrolled);
        course.setMaxStudents(maximum);
        return course;
    }
}
