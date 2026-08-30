package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.kafkaConifg;

public final class KafkaTopics {
    public static final String STUDENT_ENROLLED     = "student-enrolled";
    public static final String STUDENT_UNENROLLED   = "student-unenrolled";
    public static final String COURSE_CREATED       = "course-created";
    public static final String COURSE_DELETED       = "course-deleted";
    public static final String ANNOUNCEMENT_CREATED = "announcement-created";
    public static final String NOTIFICATION_PUSH    = "notification-push";
    public static final String FEEDBACK_CREATED     = "feedback-created";
    private KafkaTopics() {}
}
