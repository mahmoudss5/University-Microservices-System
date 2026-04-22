package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.Mappers.UpcomingEventMapper;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventRequest;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventResponse;
import UnitSystem.demo.DataAccessLayer.Entities.EventType;
import UnitSystem.demo.DataAccessLayer.Entities.UpcomingEvent;
import UnitSystem.demo.DataAccessLayer.Repositories.UpcomingEventRepository;
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
class UpcomingEventServiceImpTest {

    @Mock
    private UpcomingEventRepository upcomingEventRepository;
    @Mock
    private UpcomingEventMapper upcomingEventMapper;
    @InjectMocks
    private UpcomingEventServiceImp upcomingEventServiceImp;

    private UpcomingEvent event;
    private UpcomingEventRequest eventRequest;
    private UpcomingEventResponse eventResponse;

    @BeforeEach
    void setUp() {
        LocalDateTime eventDate = LocalDateTime.now().plusDays(7);

        event = UpcomingEvent.builder()
                .id(1L)
                .title("Spring Conference")
                .subtitle("Annual university event")
                .eventDate(eventDate)
                .type(EventType.EXAM)
                .build();

        eventRequest = UpcomingEventRequest.builder()
                .title("Spring Conference")
                .subtitle("Annual university event")
                .eventDate(eventDate)
                .type("EXAM")
                .userId(1L)
                .build();

        eventResponse = UpcomingEventResponse.builder()
                .id(1L)
                .title("Spring Conference")
                .subtitle("Annual university event")
                .eventDate(eventDate)
                .type("EXAM")
                .userId(1L)
                .build();
    }

    @Test
    void getAllEvents_returnsListOfEvents() {
        when(upcomingEventRepository.findAll()).thenReturn(List.of(event));
        when(upcomingEventMapper.mapToResponse(event)).thenReturn(eventResponse);

        List<UpcomingEventResponse> result = upcomingEventServiceImp.getAllEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Spring Conference", result.get(0).getTitle());
        verify(upcomingEventRepository).findAll();
    }

    @Test
    void getUpcomingEvents_returnsEventsAfterNow() {
        when(upcomingEventRepository.findUpcomingFromDate(any(LocalDateTime.class)))
                .thenReturn(List.of(event));
        when(upcomingEventMapper.mapToResponse(event)).thenReturn(eventResponse);

        List<UpcomingEventResponse> result = upcomingEventServiceImp.getUpcomingEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(upcomingEventRepository).findUpcomingFromDate(any(LocalDateTime.class));
    }

    @Test
    void getEventsByType_validType_returnsMatchingEvents() {
        when(upcomingEventRepository.findByType(EventType.EXAM)).thenReturn(List.of(event));
        when(upcomingEventMapper.mapToResponse(event)).thenReturn(eventResponse);

        List<UpcomingEventResponse> result = upcomingEventServiceImp.getEventsByType("EXAM");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(upcomingEventRepository).findByType(EventType.EXAM);
    }

    @Test
    void getEventsByType_invalidType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> upcomingEventServiceImp.getEventsByType("INVALID_TYPE"));
    }

    @Test
    void getEventsByUser_existingUserId_returnsUserEvents() {
        when(upcomingEventRepository.findByUserId(1L)).thenReturn(List.of(event));
        when(upcomingEventMapper.mapToResponse(event)).thenReturn(eventResponse);

        List<UpcomingEventResponse> result = upcomingEventServiceImp.getEventsByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(upcomingEventRepository).findByUserId(1L);
    }

    @Test
    void getEventById_existingId_returnsEventResponse() {
        when(upcomingEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(upcomingEventMapper.mapToResponse(event)).thenReturn(eventResponse);

        UpcomingEventResponse result = upcomingEventServiceImp.getEventById(1L);

        assertNotNull(result);
        assertEquals("Spring Conference", result.getTitle());
    }

    @Test
    void getEventById_nonExistingId_throwsException() {
        when(upcomingEventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> upcomingEventServiceImp.getEventById(99L));
    }

    @Test
    void createEvent_validRequest_savesAndReturnsResponse() {
        when(upcomingEventMapper.mapToEntity(eventRequest)).thenReturn(event);
        when(upcomingEventMapper.mapToResponse(event)).thenReturn(eventResponse);

        UpcomingEventResponse result = upcomingEventServiceImp.createEvent(eventRequest);

        assertNotNull(result);
        assertEquals("Spring Conference", result.getTitle());
        verify(upcomingEventRepository).save(event);
    }

    @Test
    void updateEvent_existingId_updatesAndReturnsResponse() {
        UpcomingEvent updatedEntity = UpcomingEvent.builder()
                .id(1L)
                .title("Updated Conference")
                .build();
        UpcomingEventRequest updateRequest = UpcomingEventRequest.builder()
                .title("Updated Conference")
                .subtitle("New subtitle")
                .eventDate(LocalDateTime.now().plusDays(10))
                .type("EXAM")
                .userId(1L)
                .build();
        UpcomingEventResponse updatedResponse = UpcomingEventResponse.builder()
                .id(1L).title("Updated Conference").build();

        when(upcomingEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(upcomingEventMapper.mapToEntity(updateRequest)).thenReturn(updatedEntity);
        when(upcomingEventMapper.mapToResponse(event)).thenReturn(updatedResponse);

        UpcomingEventResponse result = upcomingEventServiceImp.updateEvent(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Updated Conference", result.getTitle());
        verify(upcomingEventRepository).save(event);
    }

    @Test
    void updateEvent_nonExistingId_throwsException() {
        when(upcomingEventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> upcomingEventServiceImp.updateEvent(99L, eventRequest));
        verify(upcomingEventRepository, never()).save(any());
    }

    @Test
    void deleteEvent_callsRepositoryDeleteById() {
        doNothing().when(upcomingEventRepository).deleteById(1L);

        upcomingEventServiceImp.deleteEvent(1L);

        verify(upcomingEventRepository).deleteById(1L);
    }
}
