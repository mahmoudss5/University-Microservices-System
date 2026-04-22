package UnitSystem.demo.Controllers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.FeedbackService;
import UnitSystem.demo.DataAccessLayer.Dto.Feedback.FeedbackRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Feedback.FeedbackResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@Tag(name = "Feedback", description = "Endpoints for feedback management")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "Get all feedbacks")
    @GetMapping("/all")
    public ResponseEntity<List<FeedbackResponse>> getAllFeedbacks() {
        List<FeedbackResponse> feedbacks = feedbackService.getAllFeedbacks();
        return ResponseEntity.ok(feedbacks);
    }

    @Operation(summary = "Get feedbacks by role")
    @GetMapping("/role/{role}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksByRole(@PathVariable String role) {
        List<FeedbackResponse> feedbacks = feedbackService.getFeedbacksByRole(role);
        return ResponseEntity.ok(feedbacks);
    }

    @Operation(summary = "Get feedbacks by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksByUserId(@PathVariable Long userId) {
        List<FeedbackResponse> feedbacks = feedbackService.getFeedbacksByUserId(userId);
        return ResponseEntity.ok(feedbacks);
    }

    @Operation(summary = "Get feedback by ID")
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable Long id) {
        FeedbackResponse feedback = feedbackService.getFeedbackById(id);
        if (feedback != null) {
            return ResponseEntity.ok(feedback);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Create a new feedback")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<FeedbackResponse> createFeedback(@Valid @RequestBody FeedbackRequest feedbackRequest) {
        FeedbackResponse feedback = feedbackService.createFeedback(feedbackRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(feedback);
    }

    @Operation(summary = "Update an existing feedback")
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponse> updateFeedback(@PathVariable Long id,
            @Valid @RequestBody FeedbackRequest feedbackRequest) {
        FeedbackResponse feedback = feedbackService.updateFeedback(id, feedbackRequest);
        if (feedback != null) {
            return ResponseEntity.ok(feedback);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Delete a feedback")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "gel Recent feedbacks")
    @GetMapping("/recent")
    public ResponseEntity<List<FeedbackResponse>> getRecentFeedbacks() {
        List<FeedbackResponse> feedbacks = feedbackService.getRecentFeedbacks();
        return ResponseEntity.ok(feedbacks);
    }
}
