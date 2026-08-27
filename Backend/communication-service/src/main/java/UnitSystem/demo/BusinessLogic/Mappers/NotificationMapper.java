package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.Notification.Course.NotificationCourseRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.User.NotificationRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.User.NotificationResponse;
import UnitSystem.demo.DataAccessLayer.Entities.*;
import UnitSystem.demo.DataAccessLayer.Repositories.CourseRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.EnrollmentSnapshotRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import UnitSystem.demo.ExcHandler.Entites.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SOLID — Single Responsibility:
 * This class is only responsible for mapping between
 * Notification entities and DTOs.
 */
@Component
public class NotificationMapper {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentSnapshotRepository enrollmentSnapshotRepository;

    public NotificationMapper(
            UserRepository userRepository,
            CourseRepository courseRepository,
            EnrollmentSnapshotRepository enrollmentSnapshotRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentSnapshotRepository = enrollmentSnapshotRepository;
    }

    public Notification mapToNotificationEntity(NotificationRequest request) {
        userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User snapshot", request.getRecipientId()));

        return Notification.builder()
                .recipientId(request.getRecipientId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType() != null ? request.getType() : NotificationType.SYSTEM)
                .build();
    }

    public Notification buildNotificationForUser(Long recipientId, String title,
                                                  String message, NotificationType type) {
        return Notification.builder()
                .recipientId(recipientId)
                .title(title)
                .message(message)
                .type(type)
                .build();
    }

    public NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .recipientName(userRepository.findById(notification.getRecipientId())
                        .map(User::getUserName).orElse(null))
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    public List<Notification> mapCourseRequestToNotifications(NotificationCourseRequest request) {
        if (!courseRepository.existsById(request.getCourseId())) {
            throw new ResourceNotFoundException("Course snapshot", request.getCourseId());
        }
        var enrollments = enrollmentSnapshotRepository.findByCourseId(request.getCourseId());
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        NotificationType type = request.getType() != null
                ? request.getType()
                : NotificationType.ANNOUNCEMENT;

        return enrollments.stream()
                .map(enrollment -> buildNotificationForUser(
                        enrollment.getStudentId(),
                        request.getTitle(),
                        request.getMessage(),
                        type))
                .collect(Collectors.toList());
    }
}
