package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.UpcomingEventService;
import UnitSystem.demo.BusinessLogic.Mappers.UpcomingEventMapper;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventRequest;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventResponse;
import UnitSystem.demo.DataAccessLayer.Entities.EventType;
import UnitSystem.demo.DataAccessLayer.Entities.UpcomingEvent;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.UpcomingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpcomingEventServiceImp implements UpcomingEventService {

    private final UpcomingEventRepository upcomingEventRepository;
    private final UpcomingEventMapper upcomingEventMapper;

    @Override
    @Cacheable(value = "eventsCache", key = "'allEvents'")
    public List<UpcomingEventResponse> getAllEvents() {
        return upcomingEventRepository.findAll().stream()
                .map(upcomingEventMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "eventsCache", key = "'upcomingEvents'")
    public List<UpcomingEventResponse> getUpcomingEvents() {
        return upcomingEventRepository.findUpcomingFromDate(LocalDateTime.now()).stream()
                .map(upcomingEventMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "eventsCache", key = "'eventsByType:' + #type")
    public List<UpcomingEventResponse> getEventsByType(String type) {
        EventType eventType = EventType.valueOf(type.toUpperCase());
        return upcomingEventRepository.findByType(eventType).stream()
                .map(upcomingEventMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "eventsCache", key = "'eventsByUser:' + #userId")
    public List<UpcomingEventResponse> getEventsByUser(Long userId) {
        return upcomingEventRepository.findByUserId(userId).stream()
                .map(upcomingEventMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "eventsCache", key = "'eventById:' + #id")
    public UpcomingEventResponse getEventById(Long id) {
        return upcomingEventRepository.findById(id)
                .map(upcomingEventMapper::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Upcoming event not found with ID: " + id));
    }

    @Override
    @CacheEvict(value = "eventsCache", allEntries = true)
    public UpcomingEventResponse createEvent(UpcomingEventRequest request) {
        UpcomingEvent event = upcomingEventMapper.mapToEntity(request);
        upcomingEventRepository.save(event);
        return upcomingEventMapper.mapToResponse(event);
    }

    @Override
    @CacheEvict(value = "eventsCache", allEntries = true)
    public UpcomingEventResponse updateEvent(Long id, UpcomingEventRequest request) {
        UpcomingEvent existing = upcomingEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Upcoming event not found with ID: " + id));

        existing.setTitle(request.getTitle());
        existing.setSubtitle(request.getSubtitle());
        existing.setEventDate(request.getEventDate());
        existing.setType(EventType.valueOf(request.getType()));

        existing.setUser(upcomingEventMapper.mapToEntity(request).getUser());

        upcomingEventRepository.save(existing);
        return upcomingEventMapper.mapToResponse(existing);
    }

    @Override
    @CacheEvict(value = "eventsCache", allEntries = true)
    public void deleteEvent(Long id) {
        upcomingEventRepository.deleteById(id);
    }
}
