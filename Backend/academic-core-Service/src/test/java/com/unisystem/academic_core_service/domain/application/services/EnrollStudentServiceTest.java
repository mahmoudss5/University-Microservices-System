package com.unisystem.academic_core_service.domain.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.unisystem.academic_core_service.application.port.in.EnrollStudentUseCase.EnrollCommand;
import com.unisystem.academic_core_service.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.EnrollmentRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.EventPublisherPort;
import com.unisystem.academic_core_service.application.port.out.UserSnapshotRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.CoursePrerequisiteRepositoryPort;
import com.unisystem.academic_core_service.application.services.EnrollStudentService;
import com.unisystem.academic_core_service.domain.events.StudentEnrollend;
import com.unisystem.academic_core_service.domain.exceptions.AlreadyEnrolledException;
import com.unisystem.academic_core_service.domain.exceptions.CourseNotFoundException;
import com.unisystem.academic_core_service.domain.exceptions.InvalidEnrollmentException;
import com.unisystem.academic_core_service.domain.exceptions.PrerequisiteNotMetException;
import com.unisystem.academic_core_service.domain.model.CoursePrerequisite;
import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.domain.model.Enrollment;
import com.unisystem.academic_core_service.domain.model.EnrollmentStatus;
import com.unisystem.academic_core_service.domain.model.UserRole;
import com.unisystem.academic_core_service.domain.model.UserSnapshot;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EnrollStudentServiceTest {
    private final CourseRepositoryPort courses = mock(CourseRepositoryPort.class);
    private final EnrollmentRepositoryPort enrollments = mock(EnrollmentRepositoryPort.class);
    private final EventPublisherPort publisher = mock(EventPublisherPort.class);
    private final UserSnapshotRepositoryPort users = mock(UserSnapshotRepositoryPort.class);
    private final CoursePrerequisiteRepositoryPort prerequisites = mock(CoursePrerequisiteRepositoryPort.class);
    private final EnrollStudentService service = new EnrollStudentService(courses, enrollments, publisher, users, prerequisites);

    private void studentExists() {
        when(users.findById(3L)).thenReturn(Optional.of(
                new UserSnapshot(3L, "student", UserRole.STUDENT, true, java.time.LocalDateTime.now())));
        when(prerequisites.findByCourseId(7L)).thenReturn(java.util.List.of());
    }

    @Test
    void enrollsStudentUpdatesCapacityAndPublishesEvent() {
        studentExists();
        Course course = course(7L, "Networks", 2, 10);
        when(courses.findByIdWithLock(7L)).thenReturn(Optional.of(course));
        when(enrollments.findByStudentIdAndCourseId(3L, 7L)).thenReturn(Optional.empty());
        when(enrollments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

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
        studentExists();
        Course course = course(7L, "Networks", 2, 10);
        when(courses.findByIdWithLock(7L)).thenReturn(Optional.of(course));
        Enrollment existing = new Enrollment();
        existing.setStatus(EnrollmentStatus.ENROLLED);
        when(enrollments.findByStudentIdAndCourseId(3L, 7L)).thenReturn(Optional.of(existing));

        assertThrows(AlreadyEnrolledException.class, () -> service.enroll(new EnrollCommand(3L, 7L)));

        assertEquals(2, course.getEnrolledCount());
        verify(courses, never()).save(any());
        verify(enrollments, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void rejectsEnrollmentWhenCourseDoesNotExist() {
        studentExists();
        when(courses.findByIdWithLock(7L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () -> service.enroll(new EnrollCommand(3L, 7L)));

        verifyNoInteractions(enrollments, publisher);
    }

    @Test
    void rejectsEnrollmentAndReturnsEveryUnfinishedPrerequisite() {
        studentExists();
        Course requested = course(7L, "Advanced Networks", 0, 10);
        Course required = course(2L, "Network Fundamentals", 0, 10);
        required.setCourseCode("NET101");
        when(courses.findByIdWithLock(7L)).thenReturn(Optional.of(requested));
        when(prerequisites.findByCourseId(7L)).thenReturn(java.util.List.of(new CoursePrerequisite(7L, 2L)));
        when(enrollments.hasStudentCompletedCourse(3L, 2L)).thenReturn(false);
        when(courses.findByIds(java.util.List.of(2L))).thenReturn(java.util.List.of(required));

        PrerequisiteNotMetException exception = assertThrows(
                PrerequisiteNotMetException.class, () -> service.enroll(new EnrollCommand(3L, 7L)));

        assertEquals("NET101", exception.getMissingPrerequisites().getFirst().courseCode());
        assertEquals("Network Fundamentals", exception.getMissingPrerequisites().getFirst().courseName());
        verify(enrollments, never()).save(any());
        verify(courses, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void dropsExistingEnrollmentAndDecrementsCount() {
        Course course = course(7L, "Networks", 2, 10);
        Enrollment enrollment = new Enrollment();
        enrollment.setId(21L);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        when(courses.findById(7L)).thenReturn(Optional.of(course));
        when(enrollments.findByStudentIdAndCourseId(3L, 7L)).thenReturn(Optional.of(enrollment));

        service.drop(3L, 7L);

        verify(enrollments).save(enrollment);
        verify(courses).save(course);
        assertEquals(EnrollmentStatus.DROPPED, enrollment.getStatus());
        assertEquals(1, course.getEnrolledCount());
    }

    @Test
    void rejectsDropWhenEnrollmentDoesNotExist() {
        when(courses.findById(7L)).thenReturn(Optional.of(course(7L, "Networks", 2, 10)));
        when(enrollments.findByStudentIdAndCourseId(3L, 7L)).thenReturn(Optional.empty());

        assertThrows(InvalidEnrollmentException.class, () -> service.drop(3L, 7L));

        verify(enrollments, never()).save(any());
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
