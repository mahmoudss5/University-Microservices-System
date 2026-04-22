package UnitSystem.demo.DataAccessLayer.Dto.Notification.Course;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import UnitSystem.demo.DataAccessLayer.Entities.NotificationType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationCourseResponse {
    private Long id;
    private Long courseId;
    private String title;
    private boolean isRead;
    private NotificationType type;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
