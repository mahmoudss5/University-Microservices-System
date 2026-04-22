package UnitSystem.demo.Controllers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.NotificationService;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.Course.NotificationCourseRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.User.NotificationRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.User.NotificationResponse;
import UnitSystem.demo.DataAccessLayer.Entities.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "Endpoints for notification management")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Create a new notification")
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest notificationRequest) {
        NotificationResponse response = notificationService.createNotification(notificationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get a notification by ID")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all notifications for a user (newest first)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getAllNotificationsForUser(@PathVariable Long userId) {
        List<NotificationResponse> notifications = notificationService.getAllNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notifications for a user")
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotificationsForUser(@PathVariable Long userId) {
        List<NotificationResponse> notifications = notificationService.getUnreadNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get notifications for a user filtered by type")
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByType(
            @PathVariable Long userId,
            @PathVariable NotificationType type) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByType(userId, type);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Count unread notifications for a user")
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Long> countUnreadForUser(@PathVariable Long userId) {
        long count = notificationService.countUnreadForUser(userId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Mark a single notification as read")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark all notifications as read for a user")
    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Integer> markAllAsReadForUser(@PathVariable Long userId) {
        int updatedCount = notificationService.markAllAsReadForUser(userId);
        return ResponseEntity.ok(updatedCount);
    }

    @Operation(summary = "Delete a notification by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotificationById(@PathVariable Long id) {
        notificationService.deleteNotificationById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all notifications for a user")
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAllNotificationsForUser(@PathVariable Long userId) {
        notificationService.deleteAllNotificationsForUser(userId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Send a notification to a user")
    @PostMapping("/user/send")
    public ResponseEntity<Void> sendNotificationToUser(@Valid @RequestBody NotificationRequest notificationRequest) {
        notificationService.sendNotificationToUser(notificationRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Send a notification to all enrolled students in a course")
    @PostMapping("/course")
    public ResponseEntity<Void> sendNotificationToCourse(@Valid @RequestBody NotificationCourseRequest notificationRequest) {
        notificationService.sendNotificationToCourse(notificationRequest);
        return ResponseEntity.ok().build();
    }

}
