package UnitSystem.demo.DataAccessLayer.Dto.Feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String role;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
