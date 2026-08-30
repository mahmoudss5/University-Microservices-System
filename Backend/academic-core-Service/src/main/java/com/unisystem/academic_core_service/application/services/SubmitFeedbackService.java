package com.unisystem.academic_core_service.application.services;

import com.unisystem.academic_core_service.application.port.in.SubmitFeedbackUseCase;
import com.unisystem.academic_core_service.application.port.out.FeedbackRepsitoryPort;
import com.unisystem.academic_core_service.application.port.out.UserSnapshotRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.EventPublisherPort;
import com.unisystem.academic_core_service.domain.events.FeedbackCreatedEvent;
import com.unisystem.academic_core_service.domain.exceptions.UserSnapshotNotFoundException;
import com.unisystem.academic_core_service.domain.model.Feedback;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

public class SubmitFeedbackService implements SubmitFeedbackUseCase {

    private final FeedbackRepsitoryPort feedbackRepository;
    private final UserSnapshotRepositoryPort users;
    private final EventPublisherPort events;

    public SubmitFeedbackService(FeedbackRepsitoryPort feedbackRepository, UserSnapshotRepositoryPort users, EventPublisherPort events) {
        this.feedbackRepository = feedbackRepository;
        this.users = users;
        this.events = events;
    }

    @Override
    @Transactional
    public Feedback submit(FeedbackCommand cmd) {
        var user = users.findById(cmd.userId()).orElseThrow(() -> new UserSnapshotNotFoundException(cmd.userId()));
        if (!user.active()) throw new IllegalArgumentException("Inactive users cannot submit feedback");
        Feedback feedback = new Feedback();
        feedback.setId(cmd.id());
        feedback.setUserId(cmd.userId());
        feedback.setCourseId(cmd.courseId());
        feedback.setComment(cmd.comment());
        feedback.setCreatedAt(cmd.createdAt() != null ? cmd.createdAt() : LocalDateTime.now());
        Feedback saved = feedbackRepository.save(feedback);
        events.publishFeedbackCreated(new FeedbackCreatedEvent(saved.getId().toString(), saved.getUserId().toString(),
                saved.getCourseId().toString(), saved.getComment(), saved.getCreatedAt()));
        return saved;
    }
}
