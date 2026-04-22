package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.FeedbackService;
import UnitSystem.demo.BusinessLogic.Mappers.FeedbackMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Feedback.FeedbackRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Feedback.FeedbackResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Feedback;
import UnitSystem.demo.DataAccessLayer.Repositories.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImp implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;

    @Override
    @Cacheable(value = "feedbacksCache", key = "'allFeedbacks'")
    public List<FeedbackResponse> getAllFeedbacks() {
        return feedbackRepository.findAllOrderByCreatedAtDesc().stream()
                .map(feedbackMapper::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "feedbacksCache", key = "'feedbacksByRole:' + #role")
    public List<FeedbackResponse> getFeedbacksByRole(String role) {
        return feedbackRepository.findByRole(role).stream()
                .map(feedbackMapper::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "feedbacksCache", key = "'feedbacksByUser:' + #userId")
    public List<FeedbackResponse> getFeedbacksByUserId(Long userId) {
        return feedbackRepository.findByUserId(userId).stream()
                .limit(4) // Limit to the most recent 4 feedbacks
                .map(feedbackMapper::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "feedbacksCache", key = "'feedbackById:' + #feedbackId")
    public FeedbackResponse getFeedbackById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .map(feedbackMapper::mapToFeedbackResponse)
                .orElse(null);
    }

    @Override
    @CacheEvict(value = "feedbacksCache", allEntries = true)
    public FeedbackResponse createFeedback(FeedbackRequest feedbackRequest) {
        Feedback feedback = feedbackMapper.mapToFeedback(feedbackRequest);
        feedbackRepository.save(feedback);
        return feedbackMapper.mapToFeedbackResponse(feedback);
    }

    @Override
    @CacheEvict(value = "feedbacksCache", allEntries = true)
    public FeedbackResponse updateFeedback(Long feedbackId, FeedbackRequest feedbackRequest) {
        Feedback existingFeedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        existingFeedback.setRole(feedbackRequest.getRole());
        existingFeedback.setComment(feedbackRequest.getComment());

        feedbackRepository.save(existingFeedback);
        return feedbackMapper.mapToFeedbackResponse(existingFeedback);
    }

    @Override
    @Cacheable(value = "feedbacksCache", key = "'recentFeedbacks'")
    public List<FeedbackResponse> getRecentFeedbacks() {
        return feedbackRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .limit(4)
                .map(feedbackMapper::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "feedbacksCache", allEntries = true)
    public void deleteFeedback(Long feedbackId) {
        feedbackRepository.deleteById(feedbackId);
    }
}
