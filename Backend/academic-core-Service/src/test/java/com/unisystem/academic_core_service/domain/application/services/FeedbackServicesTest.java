package com.unisystem.academic_core_service.domain.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.unisystem.academic_core_service.domain.application.port.in.GetFeedBackQuery.FeedbackDTO;
import com.unisystem.academic_core_service.domain.application.port.in.SubmitFeedbackUseCase.FeedbackCommand;
import com.unisystem.academic_core_service.domain.application.port.out.FeedbackRepsitoryPort;
import com.unisystem.academic_core_service.domain.model.Feedback;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FeedbackServicesTest {
    private final FeedbackRepsitoryPort repository = mock(FeedbackRepsitoryPort.class);

    @Test
    void submitMapsCommandAndReturnsSavedFeedback() {
        SubmitFeedbackService service = new SubmitFeedbackService(repository);
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 12, 0);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Feedback result = service.submit(new FeedbackCommand(1L, 2L, 3L, "Helpful", createdAt));

        assertAll(
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals(2L, result.getUserId()),
                () -> assertEquals(3L, result.getCourseId()),
                () -> assertEquals("Helpful", result.getComment()),
                () -> assertEquals(createdAt, result.getCreatedAt()));
    }

    @Test
    void submitSuppliesTimestampWhenCommandOmitsIt() {
        SubmitFeedbackService service = new SubmitFeedbackService(repository);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDateTime before = LocalDateTime.now();

        Feedback result = service.submit(new FeedbackCommand(null, 2L, 3L, "Helpful", null));

        assertFalse(result.getCreatedAt().isBefore(before));
        assertFalse(result.getCreatedAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void queryMethodsMapDomainFeedbackToDtos() {
        GetFeedbackService service = new GetFeedbackService(repository);
        Feedback feedback = feedback(1L, 2L, 3L, "Helpful");
        when(repository.findByCourseId(3L)).thenReturn(List.of(feedback));
        when(repository.findByUserId(2L)).thenReturn(List.of(feedback));
        when(repository.findById(1L)).thenReturn(Optional.of(feedback));
        when(repository.findAll()).thenReturn(List.of(feedback));

        FeedbackDTO byCourse = service.getFeedbacksByCourseId(3L).getFirst();

        assertEquals("Helpful", byCourse.comment());
        assertEquals(1L, service.getFeedbackById(1L).orElseThrow().id());
        assertEquals(1, service.getFeedbacksByUserId(2L).size());
        assertEquals(1, service.getAllFeedbacks().size());
    }

    @Test
    void missingFeedbackReturnsEmptyOptional() {
        GetFeedbackService service = new GetFeedbackService(repository);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(service.getFeedbackById(99L).isEmpty());
    }

    private Feedback feedback(Long id, Long userId, Long courseId, String comment) {
        Feedback value = new Feedback();
        value.setId(id);
        value.setUserId(userId);
        value.setCourseId(courseId);
        value.setComment(comment);
        value.setCreatedAt(LocalDateTime.of(2026, 8, 1, 9, 0));
        return value;
    }
}
