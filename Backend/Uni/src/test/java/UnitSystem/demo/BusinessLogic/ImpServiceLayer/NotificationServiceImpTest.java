package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.Mappers.NotificationMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.Course.NotificationCourseRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.User.NotificationRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Notification.User.NotificationResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Notification;
import UnitSystem.demo.DataAccessLayer.Entities.NotificationType;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImpTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private NotificationMapper notificationMapper;
    @InjectMocks
    private NotificationServiceImp notificationServiceImp;

    private User recipient;
    private Notification notification;
    private NotificationRequest notificationRequest;
    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        recipient = new User();
        recipient.setId(1L);
        recipient.setUserName("john_doe");
        recipient.setEmail("john@example.com");

        notification = Notification.builder()
                .id(1L)
                .recipient(recipient)
                .title("New Assignment")
                .message("Assignment 3 has been posted.")
                .type(NotificationType.SYSTEM)
                .isRead(false)
                .build();

        notificationRequest = NotificationRequest.builder()
                .recipientId(1L)
                .title("New Assignment")
                .message("Assignment 3 has been posted.")
                .type(NotificationType.SYSTEM)
                .build();

        notificationResponse = NotificationResponse.builder()
                .id(1L)
                .recipientId(1L)
                .recipientName("john_doe")
                .title("New Assignment")
                .message("Assignment 3 has been posted.")
                .type("SYSTEM")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createNotification_validRequest_savesAndReturnsResponse() {
        when(notificationMapper.mapToNotificationEntity(notificationRequest)).thenReturn(notification);
        when(notificationMapper.mapToNotificationResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationServiceImp.createNotification(notificationRequest);

        assertNotNull(result);
        assertEquals("New Assignment", result.getTitle());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_existingNotification_marksAndReturnsResponse() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationMapper.mapToNotificationResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationServiceImp.markAsRead(1L);

        assertNotNull(result);
        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_nonExistingNotification_throwsException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationServiceImp.markAsRead(99L));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllAsReadForUser_callsRepositoryAndReturnsCount() {
        when(notificationRepository.markAllAsReadForUser(1L)).thenReturn(3);

        int count = notificationServiceImp.markAllAsReadForUser(1L);

        assertEquals(3, count);
        verify(notificationRepository).markAllAsReadForUser(1L);
    }

    @Test
    void deleteNotificationById_callsRepositoryDeleteById() {
        doNothing().when(notificationRepository).deleteById(1L);

        notificationServiceImp.deleteNotificationById(1L);

        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void deleteAllNotificationsForUser_callsRepositoryMethod() {
        doNothing().when(notificationRepository).deleteAllByRecipientId(1L);

        notificationServiceImp.deleteAllNotificationsForUser(1L);

        verify(notificationRepository).deleteAllByRecipientId(1L);
    }

    @Test
    void sendNotificationToUser_savesAndSendsViaWebSocket() {
        when(notificationMapper.mapToNotificationEntity(notificationRequest)).thenReturn(notification);
        when(notificationMapper.mapToNotificationResponse(notification)).thenReturn(notificationResponse);

        notificationServiceImp.sendNotificationToUser(notificationRequest);

        verify(notificationRepository).save(notification);
        verify(simpMessagingTemplate).convertAndSendToUser(
                eq("john@example.com"), eq("/queue/notifications"), eq(notificationResponse));
    }

    @Test
    void sendNotificationToCourse_withEnrolledStudents_savesAllAndSendsWebSocket() {
        NotificationCourseRequest courseRequest = mock(NotificationCourseRequest.class);
        when(courseRequest.getCourseId()).thenReturn(10L);
        when(notificationMapper.mapCourseRequestToNotifications(courseRequest))
                .thenReturn(List.of(notification));
        when(notificationMapper.mapToNotificationResponse(notification)).thenReturn(notificationResponse);

        notificationServiceImp.sendNotificationToCourse(courseRequest);

        verify(notificationRepository).saveAll(List.of(notification));
        verify(simpMessagingTemplate).convertAndSendToUser(
                eq("john@example.com"), eq("/queue/notifications"), eq(notificationResponse));
    }

    @Test
    void sendNotificationToCourse_noEnrolledStudents_doesNotSave() {
        NotificationCourseRequest courseRequest = mock(NotificationCourseRequest.class);
        when(courseRequest.getCourseId()).thenReturn(10L);
        when(notificationMapper.mapCourseRequestToNotifications(courseRequest)).thenReturn(List.of());

        notificationServiceImp.sendNotificationToCourse(courseRequest);

        verify(notificationRepository, never()).saveAll(any());
        verify(simpMessagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    void getNotificationById_existingId_returnsResponse() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationMapper.mapToNotificationResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationServiceImp.getNotificationById(1L);

        assertNotNull(result);
        assertEquals("New Assignment", result.getTitle());
    }

    @Test
    void getNotificationById_nonExistingId_throwsException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationServiceImp.getNotificationById(99L));
    }

    @Test
    void getAllNotificationsForUser_returnsListOfResponses() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));
        when(notificationMapper.mapToNotificationResponse(notification)).thenReturn(notificationResponse);

        List<NotificationResponse> result = notificationServiceImp.getAllNotificationsForUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUnreadNotificationsForUser_returnsOnlyUnread() {
        when(notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));
        when(notificationMapper.mapToNotificationResponse(notification)).thenReturn(notificationResponse);

        List<NotificationResponse> result = notificationServiceImp.getUnreadNotificationsForUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }

    @Test
    void getNotificationsByType_returnsFilteredNotifications() {
        when(notificationRepository.findByRecipientIdAndTypeOrderByCreatedAtDesc(1L, NotificationType.SYSTEM))
                .thenReturn(List.of(notification));
        when(notificationMapper.mapToNotificationResponse(notification)).thenReturn(notificationResponse);

        List<NotificationResponse> result = notificationServiceImp.getNotificationsByType(1L, NotificationType.SYSTEM);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void countUnreadForUser_returnsCorrectCount() {
        when(notificationRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(5L);

        long count = notificationServiceImp.countUnreadForUser(1L);

        assertEquals(5L, count);
    }
}
