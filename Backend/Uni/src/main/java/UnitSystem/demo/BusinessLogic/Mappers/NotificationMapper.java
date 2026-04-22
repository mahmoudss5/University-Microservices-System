package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.Notification.Course.NotificationCourseRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.User.NotificationRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.User.NotificationResponse;
import UnitSystem.demo.DataAccessLayer.Entities.EnrolledCourse;
import UnitSystem.demo.DataAccessLayer.Entities.Notification;
import UnitSystem.demo.DataAccessLayer.Entities.NotificationType;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.CourseRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public Notification mapToNotificationEntity(NotificationRequest request) {
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getRecipientId()));

        return Notification.builder()
                .recipient(recipient)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType() != null ? request.getType() : NotificationType.SYSTEM)
                .build();
    }

    public Notification buildNotificationForUser(User recipient, String title, String message, NotificationType type) {
        return Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .build();
    }

    public NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipient().getId())
                .recipientName(notification.getRecipient().getUserName())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    public List<Notification> mapCourseRequestToNotifications(NotificationCourseRequest request) {
        var course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + request.getCourseId()));

        Set<EnrolledCourse> enrolledCourses = course.getCourseEnrollments();
        if (enrolledCourses.isEmpty()) {
            return Collections.emptyList();
        }

        NotificationType type = request.getType() != null
                ? request.getType()
                : NotificationType.ANNOUNCEMENT;

        return enrolledCourses.stream()
                .map(enrollment -> buildNotificationForUser(
                        enrollment.getStudent(),
                        request.getTitle(),
                        request.getMessage(),
                        type))
                .collect(Collectors.toList());
    }
}
