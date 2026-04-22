package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.Mappers.MessageMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Message.MessageRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Message.MessageResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Entities.Message;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImpTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @InjectMocks
    private MessageServiceImp messageServiceImp;

    private Message message;
    private MessageRequest messageRequest;
    private MessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        User sender = new User();
        sender.setId(1L);
        sender.setUserName("john_doe");
        sender.setEmail("john@example.com");

        Course course = Course.builder()
                .id(10L)
                .name("Computer Science 101")
                .courseCode("CS101")
                .build();

        message = Message.builder()
                .id(1L)
                .course(course)
                .sender(sender)
                .content("When is the assignment due?")
                .createdAt(LocalDateTime.now())
                .build();

        messageRequest = MessageRequest.builder()
                .courseId(10L)
                .senderId(1L)
                .content("When is the assignment due?")
                .build();

        messageResponse = MessageResponse.builder()
                .id(1L)
                .courseId(10L)
                .courseName("Computer Science 101")
                .senderId(1L)
                .senderName("john_doe")
                .content("When is the assignment due?")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createMessage_savesMessageAndBroadcastsToCourseTopic() {
        when(messageMapper.mapToMessageEntity(messageRequest)).thenReturn(message);
        when(messageMapper.mapToMessageResponse(message)).thenReturn(messageResponse);

        messageServiceImp.createMessage(messageRequest);

        verify(messageRepository).save(message);
        verify(messagingTemplate).convertAndSend("/topic/course/10", messageResponse);
    }

    @Test
    void getMessagesByCourseId_returnsMessagesInAscendingOrder() {
        when(messageRepository.findByCourseIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(message));
        when(messageMapper.mapToMessageResponse(message)).thenReturn(messageResponse);

        List<MessageResponse> result = messageServiceImp.getMessagesByCourseId(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("When is the assignment due?", result.get(0).getContent());
        verify(messageRepository).findByCourseIdOrderByCreatedAtAsc(10L);
    }

    @Test
    void getMessagesByCourseId_noCourseMessages_returnsEmptyList() {
        when(messageRepository.findByCourseIdOrderByCreatedAtAsc(99L)).thenReturn(List.of());

        List<MessageResponse> result = messageServiceImp.getMessagesByCourseId(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getMessagesBySenderId_returnsMessagesInDescendingOrder() {
        when(messageRepository.findBySenderIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(message));
        when(messageMapper.mapToMessageResponse(message)).thenReturn(messageResponse);

        List<MessageResponse> result = messageServiceImp.getMessagesBySenderId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(messageRepository).findBySenderIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void countMessagesByCourseId_returnsCorrectCount() {
        when(messageRepository.countByCourseId(10L)).thenReturn(5L);

        long count = messageServiceImp.countMessagesByCourseId(10L);

        assertEquals(5L, count);
        verify(messageRepository).countByCourseId(10L);
    }

    @Test
    void deleteMessageById_callsRepositoryDeleteById() {
        doNothing().when(messageRepository).deleteById(1L);

        messageServiceImp.deleteMessageById(1L);

        verify(messageRepository).deleteById(1L);
    }
}
