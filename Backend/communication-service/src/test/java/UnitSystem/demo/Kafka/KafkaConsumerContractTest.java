package UnitSystem.demo.Kafka;

import static org.mockito.Mockito.*;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.NotificationService;
import UnitSystem.demo.DataAccessLayer.Repositories.CourseRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.EnrollmentSnapshotRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KafkaConsumerContractTest {
    private final NotificationService notifications = mock(NotificationService.class);
    private final UserRepository users = mock(UserRepository.class);
    private final CourseRepository courses = mock(CourseRepository.class);
    private final EnrollmentSnapshotRepository enrollments = mock(EnrollmentSnapshotRepository.class);
    private final KafkaConsumer consumer = new KafkaConsumer(notifications, users, courses, enrollments);

    @Test void consumesNewCourseCreatedEnvelope() {
        consumer.onCourseCreated(envelope("COURSE_CREATED", Map.of(
                "courseId", "12", "courseName", "Databases", "courseCode", "CS305")));
        verify(courses).save(argThat(course -> course.getCourseId().equals(12L)
                && course.getCourseName().equals("Databases")));
    }

    @Test void consumesNewStudentEnrolledEnvelope() {
        when(users.existsById(7L)).thenReturn(true);
        consumer.onStudentEnrolled(envelope("STUDENT_ENROLLED", Map.of(
                "studentId", "7", "enrolledCourseId", "12", "courseName", "Databases")));
        verify(enrollments).save(argThat(value -> value.getStudentId().equals(7L) && value.getCourseId().equals(12L)));
        verify(notifications).sendNotificationToUser(any());
    }

    @Test void keepsAcceptingLegacyFlatEventDuringMigration() {
        consumer.onCourseCreated(Map.of("courseId", "12", "courseName", "Databases"));
        verify(courses).save(any());
    }

    @Test void consumesStudentUnenrolledEnvelope() {
        consumer.onStudentUnenrolled(envelope("STUDENT_UNENROLLED", Map.of("studentId", "7", "courseId", "12")));
        verify(enrollments).deleteByStudentIdAndCourseId(7L, 12L);
    }

    @Test void consumesAnnouncementCreatedEnvelope() {
        consumer.onAnnouncementCreated(envelope("ANNOUNCEMENT_CREATED", Map.of(
                "courseId", "12", "courseName", "Databases",
                "title", "Exam", "description", "Exam moved to Monday")));
        verify(courses).save(argThat(course -> course.getCourseId().equals(12L)));
        verify(notifications).sendNotificationToCourse(any());
    }

    @Test void consumesCourseDeletedEnvelope() {
        when(enrollments.findByCourseId(12L)).thenReturn(List.of());
        consumer.onCourseDeleted(envelope("COURSE_DELETED", Map.of("courseId", "12")));
        verify(enrollments).deleteAll(List.of());
        verify(courses).deleteById(12L);
    }

    private Map<String, Object> envelope(String type, Map<String, Object> payload) {
        return Map.of("eventId", "05aa88ea-a6bb-42f0-88a8-02bb82fb62f7", "eventType", type,
                "eventVersion", 1, "occurredAt", "2026-08-30T13:55:00", "aggregateId", "12", "event", payload);
    }
}
