package com.unisystem.academic_core_service.domain.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.unisystem.academic_core_service.application.port.in.CreateCourseUseCase.CreateCourseCommand;
import com.unisystem.academic_core_service.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.EventPublisherPort;
import com.unisystem.academic_core_service.application.port.out.UserSnapshotRepositoryPort;
import com.unisystem.academic_core_service.application.services.CreateCourseService;
import com.unisystem.academic_core_service.domain.events.CourseCreatedEvent;
import com.unisystem.academic_core_service.domain.exceptions.DuplicateCourseException;
import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.domain.model.UserRole;
import com.unisystem.academic_core_service.domain.model.UserSnapshot;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateCourseServiceTest {
    private final CourseRepositoryPort repository = mock(CourseRepositoryPort.class);
    private final EventPublisherPort publisher = mock(EventPublisherPort.class);
    private final UserSnapshotRepositoryPort users = mock(UserSnapshotRepositoryPort.class);
    private final CreateCourseService service = new CreateCourseService(repository, publisher, users);

    private void teacherExists() {
        when(users.findById(9L)).thenReturn(java.util.Optional.of(
                new UserSnapshot(9L, "teacher", UserRole.TEACHER, true, java.time.LocalDateTime.now())));
    }

    @Test
    void createsCourseAndPublishesEvent() {
        teacherExists();
        CreateCourseCommand command = new CreateCourseCommand(
                "Algorithms", "CS301", "Core algorithms", 40, 3, 2L, 9L,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 20));
        when(repository.existsByCourseCode("CS301")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(11L);
            return course;
        });

        Course result = service.create(command);

        assertAll(
                () -> assertEquals(11L, result.getId()),
                () -> assertEquals("Algorithms", result.getName()),
                () -> assertEquals(0, result.getEnrolledCount()),
                () -> assertNotNull(result.getCreatedAt()));
        ArgumentCaptor<CourseCreatedEvent> event = ArgumentCaptor.forClass(CourseCreatedEvent.class);
        verify(publisher).publishCourseCreated(event.capture());
        assertEquals("11", event.getValue().courseId());
        assertEquals("CS301", event.getValue().courseCode());
    }

    @Test
    void rejectsDuplicateCourseCodeWithoutSaving() {
        teacherExists();
        when(repository.existsByCourseCode("CS301")).thenReturn(true);

        assertThrows(DuplicateCourseException.class, () -> service.create(commandWithDates(null, null)));

        verify(repository, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        teacherExists();
        CreateCourseCommand command = commandWithDates(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 1));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(command));

        assertEquals("End date cannot be before start date", exception.getMessage());
        verify(repository, never()).save(any());
    }

    private CreateCourseCommand commandWithDates(LocalDate start, LocalDate end) {
        return new CreateCourseCommand("Algorithms", "CS301", "Core algorithms", 40, 3, 2L, 9L, start, end);
    }
}
