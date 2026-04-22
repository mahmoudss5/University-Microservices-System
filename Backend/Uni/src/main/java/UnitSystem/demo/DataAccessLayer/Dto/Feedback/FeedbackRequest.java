package UnitSystem.demo.DataAccessLayer.Dto.Feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackRequest {
    private Long userId;
    private String role;
    private String comment;
}
