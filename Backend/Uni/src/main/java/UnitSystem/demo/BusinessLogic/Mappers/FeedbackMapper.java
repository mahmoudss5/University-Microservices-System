package UnitSystem.demo.BusinessLogic.Mappers;
import UnitSystem.demo.DataAccessLayer.Dto.Feedback.FeedbackRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Feedback.FeedbackResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Feedback;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackMapper {

    private final UserRepository userRepository;

    public FeedbackResponse mapToFeedbackResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .userId(feedback.getUser().getId())
                .userName(feedback.getUser().getUserName())
                .role(feedback.getRole())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }

    public Feedback mapToFeedback(FeedbackRequest feedbackRequest) {
        User user = userRepository.findById(feedbackRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return Feedback.builder()
                .user(user)
                .role(feedbackRequest.getRole())
                .comment(feedbackRequest.getComment())
                .build();
    }
}
