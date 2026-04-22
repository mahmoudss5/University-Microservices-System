package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.Mappers.FeedbackMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Feedback.FeedbackRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Feedback.FeedbackResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Feedback;
import UnitSystem.demo.DataAccessLayer.Repositories.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImpTest {

    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private FeedbackMapper feedbackMapper;
    @InjectMocks
    private FeedbackServiceImp feedbackServiceImp;

    private Feedback feedback;
    private FeedbackRequest feedbackRequest;
    private FeedbackResponse feedbackResponse;

    @BeforeEach
    void setUp() {
        feedback = Feedback.builder()
                .id(1L)
                .role("Student")
                .comment("Great course!")
                .build();

        feedbackRequest = FeedbackRequest.builder()
                .userId(1L)
                .role("Student")
                .comment("Great course!")
                .build();

        feedbackResponse = FeedbackResponse.builder()
                .id(1L)
                .userId(1L)
                .userName("john_doe")
                .role("Student")
                .comment("Great course!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllFeedbacks_returnsListOfFeedbacks() {
        when(feedbackRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(feedback));
        when(feedbackMapper.mapToFeedbackResponse(feedback)).thenReturn(feedbackResponse);

        List<FeedbackResponse> result = feedbackServiceImp.getAllFeedbacks();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(feedbackRepository).findAllOrderByCreatedAtDesc();
    }

    @Test
    void getFeedbacksByRole_existingRole_returnsFeedbacks() {
        when(feedbackRepository.findByRole("Student")).thenReturn(List.of(feedback));
        when(feedbackMapper.mapToFeedbackResponse(feedback)).thenReturn(feedbackResponse);

        List<FeedbackResponse> result = feedbackServiceImp.getFeedbacksByRole("Student");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Student", result.get(0).getRole());
    }

    @Test
    void getFeedbacksByUserId_returnsUpToFourFeedbacks() {
        Feedback fb2 = Feedback.builder().id(2L).role("Student").comment("Nice!").build();
        Feedback fb3 = Feedback.builder().id(3L).role("Student").comment("Good!").build();
        Feedback fb4 = Feedback.builder().id(4L).role("Student").comment("Average").build();
        Feedback fb5 = Feedback.builder().id(5L).role("Student").comment("Ok").build();
        FeedbackResponse r2 = FeedbackResponse.builder().id(2L).build();
        FeedbackResponse r3 = FeedbackResponse.builder().id(3L).build();
        FeedbackResponse r4 = FeedbackResponse.builder().id(4L).build();
        FeedbackResponse r5 = FeedbackResponse.builder().id(5L).build();

        when(feedbackRepository.findByUserId(1L)).thenReturn(List.of(feedback, fb2, fb3, fb4, fb5));
        when(feedbackMapper.mapToFeedbackResponse(feedback)).thenReturn(feedbackResponse);
        when(feedbackMapper.mapToFeedbackResponse(fb2)).thenReturn(r2);
        when(feedbackMapper.mapToFeedbackResponse(fb3)).thenReturn(r3);
        when(feedbackMapper.mapToFeedbackResponse(fb4)).thenReturn(r4);

        List<FeedbackResponse> result = feedbackServiceImp.getFeedbacksByUserId(1L);

        assertNotNull(result);
        assertEquals(4, result.size());
    }

    @Test
    void getFeedbackById_existingId_returnsFeedbackResponse() {
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(feedbackMapper.mapToFeedbackResponse(feedback)).thenReturn(feedbackResponse);

        FeedbackResponse result = feedbackServiceImp.getFeedbackById(1L);

        assertNotNull(result);
        assertEquals("Great course!", result.getComment());
    }

    @Test
    void getFeedbackById_nonExistingId_returnsNull() {
        when(feedbackRepository.findById(99L)).thenReturn(Optional.empty());

        FeedbackResponse result = feedbackServiceImp.getFeedbackById(99L);

        assertNull(result);
    }

    @Test
    void createFeedback_validRequest_savesAndReturnsResponse() {
        when(feedbackMapper.mapToFeedback(feedbackRequest)).thenReturn(feedback);
        when(feedbackMapper.mapToFeedbackResponse(feedback)).thenReturn(feedbackResponse);

        FeedbackResponse result = feedbackServiceImp.createFeedback(feedbackRequest);

        assertNotNull(result);
        assertEquals("Great course!", result.getComment());
        verify(feedbackRepository).save(feedback);
    }

    @Test
    void updateFeedback_existingId_updatesAndReturnsResponse() {
        FeedbackRequest updateRequest = FeedbackRequest.builder()
                .role("Teacher")
                .comment("Updated comment")
                .build();
        FeedbackResponse updatedResponse = FeedbackResponse.builder()
                .id(1L).role("Teacher").comment("Updated comment").build();

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(feedbackMapper.mapToFeedbackResponse(feedback)).thenReturn(updatedResponse);

        FeedbackResponse result = feedbackServiceImp.updateFeedback(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Teacher", result.getRole());
        verify(feedbackRepository).save(feedback);
    }

    @Test
    void updateFeedback_nonExistingId_throwsException() {
        when(feedbackRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> feedbackServiceImp.updateFeedback(99L, feedbackRequest));
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void getRecentFeedbacks_returnsUpToFourMostRecent() {
        Feedback fb2 = Feedback.builder().id(2L).build();
        Feedback fb3 = Feedback.builder().id(3L).build();
        Feedback fb4 = Feedback.builder().id(4L).build();
        Feedback fb5 = Feedback.builder().id(5L).build();

        when(feedbackRepository.findAllOrderByCreatedAtDesc())
                .thenReturn(List.of(feedback, fb2, fb3, fb4, fb5));
        when(feedbackMapper.mapToFeedbackResponse(feedback)).thenReturn(feedbackResponse);
        when(feedbackMapper.mapToFeedbackResponse(fb2)).thenReturn(FeedbackResponse.builder().id(2L).build());
        when(feedbackMapper.mapToFeedbackResponse(fb3)).thenReturn(FeedbackResponse.builder().id(3L).build());
        when(feedbackMapper.mapToFeedbackResponse(fb4)).thenReturn(FeedbackResponse.builder().id(4L).build());

        List<FeedbackResponse> result = feedbackServiceImp.getRecentFeedbacks();

        assertEquals(4, result.size());
    }

    @Test
    void deleteFeedback_callsRepositoryDeleteById() {
        doNothing().when(feedbackRepository).deleteById(1L);

        feedbackServiceImp.deleteFeedback(1L);

        verify(feedbackRepository).deleteById(1L);
    }
}
