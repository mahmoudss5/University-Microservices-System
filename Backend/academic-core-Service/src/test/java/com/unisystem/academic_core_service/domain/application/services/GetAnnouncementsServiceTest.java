package com.unisystem.academic_core_service.domain.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.unisystem.academic_core_service.domain.application.port.in.GetAnnouncementsQuery.AnnouncementDTO;
import com.unisystem.academic_core_service.domain.application.port.out.AnnouncementRepositoryPort;
import com.unisystem.academic_core_service.domain.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.domain.application.port.out.EnrollmentRepositoryPort;
import com.unisystem.academic_core_service.domain.model.Announcement;
import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.domain.model.Enrollment;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetAnnouncementsServiceTest {
    private final AnnouncementRepositoryPort announcements = mock(AnnouncementRepositoryPort.class);
    private final EnrollmentRepositoryPort enrollments = mock(EnrollmentRepositoryPort.class);
    private final CourseRepositoryPort courses = mock(CourseRepositoryPort.class);
    private final GetAnnouncementsService service = new GetAnnouncementsService(announcements, enrollments, courses);

    @Test
    void mapsAnnouncementsForCourse() {
        Announcement announcement = announcement(1L, 4L, "Welcome", LocalDateTime.of(2026, 8, 1, 9, 0));
        when(announcements.findByCourseId(4L)).thenReturn(List.of(announcement));

        List<AnnouncementDTO> result = service.getAnnouncementsByCourseId(4L);

        assertEquals(1, result.size());
        assertEquals("Welcome", result.getFirst().title());
        assertEquals(4L, result.getFirst().courseId());
    }

    @Test
    void combinesDistinctStudentCoursesAndSortsNewestFirstWithNullLast() {
        Enrollment first = enrollment(10L);
        Enrollment duplicate = enrollment(10L);
        Enrollment second = enrollment(20L);
        Enrollment missingCourse = enrollment(null);
        when(enrollments.findByStudentId(3L)).thenReturn(List.of(first, duplicate, second, missingCourse));
        when(announcements.findByCourseId(10L)).thenReturn(List.of(
                announcement(1L, 10L, "Old", LocalDateTime.of(2026, 8, 1, 9, 0)),
                announcement(2L, 10L, "Undated", null)));
        when(announcements.findByCourseId(20L)).thenReturn(List.of(
                announcement(3L, 20L, "New", LocalDateTime.of(2026, 8, 20, 9, 0))));

        List<AnnouncementDTO> result = service.getAnnouncementsByStudentId(3L);

        assertEquals(List.of("New", "Old", "Undated"), result.stream().map(AnnouncementDTO::title).toList());
        verify(announcements, times(1)).findByCourseId(10L);
    }

    @Test
    void getsAnnouncementsAcrossTeachersDistinctCourses() {
        Course first = new Course();
        first.setId(10L);
        Course duplicate = new Course();
        duplicate.setId(10L);
        Course withoutId = new Course();
        when(courses.findByTeacherId(8L)).thenReturn(List.of(first, duplicate, withoutId));
        when(announcements.findByCourseId(10L)).thenReturn(List.of(announcement(1L, 10L, "Only", null)));

        List<AnnouncementDTO> result = service.getAnnouncementsByTeacherId(8L);

        assertEquals(1, result.size());
        verify(announcements, times(1)).findByCourseId(10L);
    }

    private Announcement announcement(Long id, Long courseId, String title, LocalDateTime createdAt) {
        Announcement value = new Announcement();
        value.setId(id);
        value.setCourseId(courseId);
        value.setTitle(title);
        value.setContent("content");
        value.setCreatedAt(createdAt);
        return value;
    }

    private Enrollment enrollment(Long courseId) {
        Enrollment value = new Enrollment();
        value.setCourseId(courseId);
        return value;
    }
}
