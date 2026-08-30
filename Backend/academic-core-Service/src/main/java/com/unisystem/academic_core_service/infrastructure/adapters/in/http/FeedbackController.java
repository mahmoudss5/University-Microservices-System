package com.unisystem.academic_core_service.infrastructure.adapters.in.http;

import com.unisystem.academic_core_service.application.port.in.GetFeedBackQuery;
import com.unisystem.academic_core_service.application.port.in.SubmitFeedbackUseCase;
import com.unisystem.academic_core_service.domain.model.Feedback;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.services.FeedbackHttpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackHttpService feedbackHttpService;

    @PostMapping
    public ResponseEntity<Feedback> submitFeedback(@RequestBody SubmitFeedbackUseCase.FeedbackCommand command) {
        return ResponseEntity.ok(feedbackHttpService.submitFeedback(command));
    }

    @GetMapping
    public ResponseEntity<List<GetFeedBackQuery.FeedbackDTO>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackHttpService.getAllFeedbacks());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<GetFeedBackQuery.FeedbackDTO>> getRecentFeedbacks() {
        return ResponseEntity.ok(feedbackHttpService.getRecentFeedbacks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetFeedBackQuery.FeedbackDTO> getFeedbackById(@PathVariable Long id) {
        return feedbackHttpService.getFeedbackById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<GetFeedBackQuery.FeedbackDTO>> getFeedbackByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(feedbackHttpService.getFeedbackByCourseId(courseId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GetFeedBackQuery.FeedbackDTO>> getFeedbackByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(feedbackHttpService.getFeedbackByUserId(userId));
    }
}
