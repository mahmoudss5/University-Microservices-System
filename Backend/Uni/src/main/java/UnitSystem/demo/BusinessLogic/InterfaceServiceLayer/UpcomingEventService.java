package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;

import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventRequest;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventResponse;

import java.util.List;

public interface UpcomingEventService {

    List<UpcomingEventResponse> getAllEvents();

    List<UpcomingEventResponse> getUpcomingEvents();

    List<UpcomingEventResponse> getEventsByType(String type);

    List<UpcomingEventResponse> getEventsByUser(Long userId);

    UpcomingEventResponse getEventById(Long id);

    UpcomingEventResponse createEvent(UpcomingEventRequest request);

    UpcomingEventResponse updateEvent(Long id, UpcomingEventRequest request);

    void deleteEvent(Long id);
}
