package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;

import UnitSystem.demo.DataAccessLayer.Dto.Feedback.FeedbackRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Feedback.FeedbackResponse;

import java.util.List;

public interface FeedbackService {
    List<FeedbackResponse> getAllFeedbacks();

    List<FeedbackResponse> getFeedbacksByRole(String role);

    List<FeedbackResponse> getFeedbacksByUserId(Long userId);

    FeedbackResponse getFeedbackById(Long feedbackId);

    FeedbackResponse createFeedback(FeedbackRequest feedbackRequest);

    FeedbackResponse updateFeedback(Long feedbackId, FeedbackRequest feedbackRequest);
    List<FeedbackResponse> getRecentFeedbacks();
    void deleteFeedback(Long feedbackId);
}
