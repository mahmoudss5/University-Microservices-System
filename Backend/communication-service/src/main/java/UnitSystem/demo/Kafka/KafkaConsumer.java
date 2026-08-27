package UnitSystem.demo.Kafka;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.NotificationService;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.Course.NotificationCourseRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.User.NotificationRequest;
import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Entities.EnrollmentSnapshot;
import UnitSystem.demo.DataAccessLayer.Entities.NotificationType;
import UnitSystem.demo.DataAccessLayer.Entities.Role;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.CourseRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.EnrollmentSnapshotRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentSnapshotRepository enrollmentSnapshotRepository;

    // ── Topic: student-enrolled ───────────────────────────
    // Sent by Academic Core when a student enrolls in a course
    @KafkaListener(topics = "student-enrolled", groupId = "communication-group")
    public void onStudentEnrolled(Map<String, Object> event) {
        log.info("Received student-enrolled event: {}", event);
        try {
            Long studentId = toLong(event.get("studentId"));
            Long courseId = toLong(event.get("enrolledCourseId"));
            String courseName = requiredString(event, "courseName");

            // The event contains enough course data to repair a missed course-created event.
            upsertCourseSnapshot(courseId, courseName);
            if (!userRepository.existsById(studentId)) {
                throw new IllegalStateException(
                        "Cannot save enrollment snapshot: user snapshot does not exist for " + studentId);
            }
            if (!enrollmentSnapshotRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
                enrollmentSnapshotRepository.save(EnrollmentSnapshot.builder()
                        .studentId(studentId)
                        .courseId(courseId)
                        .build());
            }

            NotificationRequest request = NotificationRequest.builder()
                    .recipientId(studentId)
                    .title("Enrollment Confirmed ✅")
                    .message("You have successfully enrolled in \"" + courseName + "\". Good luck!")
                    .type(NotificationType.ENROLLMENT)
                    .build();

            notificationService.sendNotificationToUser(request);
        } catch (Exception e) {
            log.error("Error handling student-enrolled event: {}", e.getMessage(), e);
        }
    }
    @KafkaListener(topics = "announcement-created", groupId = "communication-group")
    public void onAnnouncementCreated(Map<String, Object> event) {
        log.info("Received announcement-created event for course ID: {}", event.get("courseId"));
        try {
            Long courseId = toLong(event.get("courseId"));
            String courseNameStr = requiredString(event, "courseName");
            String title = requiredString(event, "title");
            String description = requiredString(event, "description");
           // save a copy of the course in the local database
            upsertCourseSnapshot(courseId, courseNameStr);

            NotificationCourseRequest request = NotificationCourseRequest.builder()
                    .courseId(courseId)
                    .title("New Announcement in " + courseNameStr + " 📢")
                    .message(title + ": " + description)
                    .type(NotificationType.ANNOUNCEMENT)
                    .build();

            notificationService.sendNotificationToCourse(request);
        } catch (Exception e) {
            log.error("Error handling announcement-created event: {}", e.getMessage(), e);
        }
    }

    // ── Topic: course-created ─────────────────────────────
    // Sent by Academic Core when a new course is created
    @KafkaListener(topics = "course-created", groupId = "communication-group")
    public void onCourseCreated(Map<String, Object> event) {
        log.info("Received course-created event: {}", event);
        try {
            Long courseId = toLong(event.get("courseId"));
            String courseName = requiredString(event, "courseName");
            upsertCourseSnapshot(courseId, courseName);
        } catch (Exception e) {
            log.error("Error handling course-created event: {}", e.getMessage(), e);
        }
    }

    // ── Topic: notification-push ──────────────────────────
    // Generic push topic — any service can use this to push a notification
    @KafkaListener(topics = "notification-push", groupId = "communication-group")
    public void onNotificationPush(Map<String, Object> event) {
        log.info("Received notification-push event: {}", event);
        try {
            Long recipientId = toLong(event.get("recipientId"));
            String title = requiredString(event, "title");
            String message = requiredString(event, "message");
            String typeStr = event.containsKey("type")
                    ? String.valueOf(event.get("type"))
                    : "SYSTEM";

            NotificationRequest request = NotificationRequest.builder()
                    .recipientId(recipientId)
                    .title(title)
                    .message(message)
                    .type(NotificationType.valueOf(typeStr))
                    .build();

            notificationService.sendNotificationToUser(request);
        } catch (Exception e) {
            log.error("Error handling notification-push event: {}", e.getMessage(), e);
        }
    }

    // ── Topic: student-registered ─────────────────────────
    // Sent by IAM Service when a new user registers
    @KafkaListener(topics = "student-registered", groupId = "communication-group")
    public void onStudentRegistered(Map<String, Object> event) {
        log.info("Received student-registered event: {}", event);
        try {
            Long userId = toLong(event.get("userId"));
            String username = requiredString(event, "username");
            Role role = Role.valueOf(requiredString(event, "role").toUpperCase());

            userRepository.save(User.builder()
                    .userId(userId)
                    .userName(username)
                    .userRole(role)
                    .build());

            NotificationRequest request = NotificationRequest.builder()
                    .recipientId(userId)
                    .title("Welcome to Uni-System! 🎉")
                    .message("Hello " + username + ", your account has been successfully created.")
                    .type(NotificationType.SYSTEM)
                    .build();

            notificationService.sendNotificationToUser(request);
        } catch (Exception e) {
            log.error("Error handling student-registered event: {}", e.getMessage(), e);
        }
    }

    // ── Helper ────────────────────────────────────────────

    private Long toLong(Object value) {
        if (value == null) throw new IllegalArgumentException("Value is null");
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        return Long.parseLong(value.toString());
    }

    private String requiredString(Map<String, Object> event, String field) {
        Object value = event.get(field);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Missing required event field: " + field);
        }
        return value.toString();
    }

    private void upsertCourseSnapshot(Long courseId, String courseName) {
        courseRepository.save(Course.builder()
                .courseId(courseId)
                .courseName(courseName)
                .build());
    }
}
