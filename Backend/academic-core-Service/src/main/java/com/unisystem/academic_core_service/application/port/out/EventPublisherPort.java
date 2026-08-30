package com.unisystem.academic_core_service.application.port.out;

import com.unisystem.academic_core_service.domain.events.AnnouncementCreatedEvent;
import com.unisystem.academic_core_service.domain.events.CourseCreatedEvent;
import com.unisystem.academic_core_service.domain.events.StudentEnrollend;
import com.unisystem.academic_core_service.domain.events.StudentUnenrolledEvent;
import com.unisystem.academic_core_service.domain.events.CourseDeletedEvent;
import com.unisystem.academic_core_service.domain.events.FeedbackCreatedEvent;

public interface EventPublisherPort {
    void publishStudentEnrolled(StudentEnrollend event);
    void publishStudentUnenrolled(StudentUnenrolledEvent event);
    void publishCourseCreated(CourseCreatedEvent event);
    void publishCourseDeleted(CourseDeletedEvent event);
    void publishAnnouncementCreated(AnnouncementCreatedEvent event);
    void publishFeedbackCreated(FeedbackCreatedEvent event);
}
