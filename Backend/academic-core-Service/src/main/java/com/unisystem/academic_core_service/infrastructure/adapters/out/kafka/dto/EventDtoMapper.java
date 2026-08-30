package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.dto;

import com.unisystem.academic_core_service.domain.events.AnnouncementCreatedEvent;
import com.unisystem.academic_core_service.domain.events.CourseCreatedEvent;
import com.unisystem.academic_core_service.domain.events.StudentEnrollend;

public final class EventDtoMapper {

    private EventDtoMapper() {
    }

    public static StudentEnrolledEventDto toDto(StudentEnrollend event) {
        return new StudentEnrolledEventDto(event.studentId(), event.enrolledCourseId(), event.courseName());
    }

    public static CourseCreatedEventDto toDto(CourseCreatedEvent event) {
        return new CourseCreatedEventDto(
                event.courseId(), event.courseName(), event.courseCode(), event.createdAt());
    }

    public static AnnouncementCreatedEventDto toDto(AnnouncementCreatedEvent event) {
        return new AnnouncementCreatedEventDto(
                event.id(),
                event.courseId(),
                event.courseName(),
                event.title(),
                event.description(),
                event.createdAt());
    }

    public static StudentUnenrolledEventDto toDto(com.unisystem.academic_core_service.domain.events.StudentUnenrolledEvent event) {
        return new StudentUnenrolledEventDto(event.studentId(), event.courseId());
    }

    public static CourseDeletedEventDto toDto(com.unisystem.academic_core_service.domain.events.CourseDeletedEvent event) {
        return new CourseDeletedEventDto(event.courseId());
    }

    public static FeedbackCreatedEventDto toDto(com.unisystem.academic_core_service.domain.events.FeedbackCreatedEvent event) {
        return new FeedbackCreatedEventDto(
                event.feedbackId(),
                event.studentId(),
                event.courseId(),
                event.comment(),
                event.createdAt());
    }
}
