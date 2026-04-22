package UnitSystem.demo.DataAccessLayer.Dto.Notification.Course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import UnitSystem.demo.DataAccessLayer.Entities.NotificationType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationCourseRequest {
    private Long courseId;
    private String title;

    private String message;
    @Builder.Default
    private NotificationType type = NotificationType.SYSTEM;
}
