package com.unisystem.academic_core_service.domain.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.unisystem.academic_core_service.application.port.in.CreateAnnouncementUseCase.CreateAnnouncementCommand;
import com.unisystem.academic_core_service.application.port.out.AnnouncementRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.EventPublisherPort;
import com.unisystem.academic_core_service.application.services.CreateAnnouncementService;
import com.unisystem.academic_core_service.domain.events.AnnouncementCreatedEvent;
import com.unisystem.academic_core_service.domain.exceptions.CourseNotFoundException;
import com.unisystem.academic_core_service.domain.model.Announcement;
import com.unisystem.academic_core_service.domain.model.Course;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateAnnouncementServiceTest {
    private final AnnouncementRepositoryPort announcements = mock(AnnouncementRepositoryPort.class);
    private final CourseRepositoryPort courses = mock(CourseRepositoryPort.class);
    private final EventPublisherPort publisher = mock(EventPublisherPort.class);
    private final CreateAnnouncementService service = new CreateAnnouncementService(announcements, courses, publisher);

    @Test
    void createsAnnouncementAndPublishesCourseContext() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 10, 30);
        Course course = new Course();
        course.setId(5L);
        course.setName("Databases");
        when(courses.findById(5L)).thenReturn(Optional.of(course));
        when(announcements.save(any())).thenAnswer(invocation -> {
            Announcement announcement = invocation.getArgument(0);
            announcement.setId(17L);
            return announcement;
        });

        Announcement result = service.create(new CreateAnnouncementCommand("Exam", "Room 4", 5L, createdAt));

        assertEquals(17L, result.getId());
        assertEquals(createdAt, result.getCreatedAt());
        ArgumentCaptor<AnnouncementCreatedEvent> event = ArgumentCaptor.forClass(AnnouncementCreatedEvent.class);
        verify(publisher).publishAnnouncementCreated(event.capture());
        assertAll(
                () -> assertEquals("17", event.getValue().id()),
                () -> assertEquals("Databases", event.getValue().courseName()),
                () -> assertEquals("Exam", event.getValue().title()));
    }

    @Test
    void rejectsMissingCourseIdBeforeRepositoryAccess() {
        assertThrows(CourseNotFoundException.class,
                () -> service.create(new CreateAnnouncementCommand("Title", "Body", null, null)));

        verifyNoInteractions(courses, announcements, publisher);
    }

    @Test
    void rejectsUnknownCourse() {
        when(courses.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class,
                () -> service.create(new CreateAnnouncementCommand("Title", "Body", 99L, null)));

        verifyNoInteractions(announcements, publisher);
    }
}
