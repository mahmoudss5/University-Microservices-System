package com.AuditLog.AuditLogService.DataAccessLayer.Entities;

/**
 * Event types currently published by the Gateway, IAM, Academic Core, and
 * Communication services.
 */
public enum AuditEventType {
    UNKNOWN,
    RATE_LIMIT_EXCEEDED,
    USER_REGISTERED,
    USER_UPDATED,
    USER_DEACTIVATED,
    USER_DELETED,
    STUDENT_REGISTERED,
    STUDENT_ENROLLED,
    STUDENT_UNENROLLED,
    COURSE_CREATED,
    COURSE_DELETED,
    ANNOUNCEMENT_CREATED,
    FEEDBACK_CREATED,
    NOTIFICATION_PUSH
}
