package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;

import UnitSystem.demo.DataAccessLayer.Dto.Notification.User.NotificationRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.User.NotificationResponse;
import UnitSystem.demo.DataAccessLayer.Entities.NotificationType;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.Course.NotificationCourseRequest;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest notificationRequest);

    NotificationResponse getNotificationById(Long notificationId);

    List<NotificationResponse> getAllNotificationsForUser(Long userId);

    List<NotificationResponse> getUnreadNotificationsForUser(Long userId);

    List<NotificationResponse> getNotificationsByType(Long userId, NotificationType type);

    long countUnreadForUser(Long userId);

    NotificationResponse markAsRead(Long notificationId);

    int markAllAsReadForUser(Long userId);

    void deleteNotificationById(Long notificationId);

    void deleteAllNotificationsForUser(Long userId);

    void sendNotificationToUser(NotificationRequest notificationRequest);

    void sendNotificationToCourse(NotificationCourseRequest notificationRequest);


}
