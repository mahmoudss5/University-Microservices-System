package com.unisystem.academic_core_service.infrastructure.adapters.in.http.services;

import com.unisystem.academic_core_service.application.port.in.GetFeedBackQuery;
import com.unisystem.academic_core_service.application.port.in.SubmitFeedbackUseCase;
import com.unisystem.academic_core_service.domain.model.Feedback;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackHttpService {

    private final SubmitFeedbackUseCase submitFeedbackUseCase;
    private final GetFeedBackQuery getFeedBackQuery;

    public Feedback submitFeedback(SubmitFeedbackUseCase.FeedbackCommand command) {
        return submitFeedbackUseCase.submit(command);
    }

    public List<GetFeedBackQuery.FeedbackDTO> getAllFeedbacks() {
        return getFeedBackQuery.getAllFeedbacks();
    }

    public List<GetFeedBackQuery.FeedbackDTO> getRecentFeedbacks() {
        return getFeedBackQuery.getAllFeedbacks().stream()
                .sorted(Comparator.comparing(GetFeedBackQuery.FeedbackDTO::createdAt).reversed())
                .limit(6)
                .toList();
    }

    public Optional<GetFeedBackQuery.FeedbackDTO> getFeedbackById(Long id) {
        return getFeedBackQuery.getFeedbackById(id);
    }

    public List<GetFeedBackQuery.FeedbackDTO> getFeedbackByCourseId(Long courseId) {
        return getFeedBackQuery.getFeedbacksByCourseId(courseId);
    }

    public List<GetFeedBackQuery.FeedbackDTO> getFeedbackByUserId(Long userId) {
        return getFeedBackQuery.getFeedbacksByUserId(userId);
    }
}
