package com.unisystem.academic_core_service.domain.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.unisystem.academic_core_service.domain.application.port.in.CreateCourseUseCase.CreateCourseCommand;
import com.unisystem.academic_core_service.domain.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.domain.application.port.out.EventPublisherPort;
import com.unisystem.academic_core_service.domain.events.CourseCreatedEvent;
import com.unisystem.academic_core_service.domain.exceptions.DuplicateCourseException;
import com.unisystem.academic_core_service.domain.model.Course;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateCourseServiceTest {
    private final CourseRepositoryPort repository = mock(CourseRepositoryPort.class);
    private final EventPublisherPort publisher = mock(EventPublisherPort.class);
    private final CreateCourseService service = new CreateCourseService(repository, publisher);

    @Test
    void createsCourseAndPublishesEvent() {
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
        assertEquals("CS301", event.getValue().departmentName());
    }

    @Test
    void rejectsDuplicateCourseCodeWithoutSaving() {
        when(repository.existsByCourseCode("CS301")).thenReturn(true);

        assertThrows(DuplicateCourseException.class, () -> service.create(commandWithDates(null, null)));

        verify(repository, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        CreateCourseCommand command = commandWithDates(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 1));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(command));

        assertEquals("End date cannot be before start date", exception.getMessage());
        verify(repository, never()).save(any());
    }

    private CreateCourseCommand commandWithDates(LocalDate start, LocalDate end) {
        return new CreateCourseCommand("Algorithms", "CS301", "Core algorithms", 40, 3, 2L, 9L, start, end);
    }
}
