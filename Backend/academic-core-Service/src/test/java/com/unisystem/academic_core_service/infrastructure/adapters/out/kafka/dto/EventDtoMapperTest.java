package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.unisystem.academic_core_service.domain.events.AnnouncementCreatedEvent;
import com.unisystem.academic_core_service.domain.events.CourseCreatedEvent;
import com.unisystem.academic_core_service.domain.events.StudentEnrollend;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EventDtoMapperTest {

    @Test
    void mapsStudentEnrolledEvent() {
        var dto = EventDtoMapper.toDto(new StudentEnrollend("7", "12", "Databases"));

        assertEquals(new StudentEnrolledEventDto("7", "12", "Databases"), dto);
    }

    @Test
    void mapsCourseCreatedEvent() {
        LocalDate createdAt = LocalDate.of(2026, 8, 29);

        var dto = EventDtoMapper.toDto(new CourseCreatedEvent("12", "Databases", "CS305", createdAt));

        assertEquals(new CourseCreatedEventDto("12", "Databases", "CS305", createdAt), dto);
    }

    @Test
    void mapsAnnouncementCreatedEvent() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 29, 10, 30);
        var event = new AnnouncementCreatedEvent(
                "3", "12", "Databases", "Exam", "Exam moved to Monday", createdAt);

        var dto = EventDtoMapper.toDto(event);

        assertEquals(
                new AnnouncementCreatedEventDto(
                        "3", "12", "Databases", "Exam", "Exam moved to Monday", createdAt),
                dto);
    }
}
