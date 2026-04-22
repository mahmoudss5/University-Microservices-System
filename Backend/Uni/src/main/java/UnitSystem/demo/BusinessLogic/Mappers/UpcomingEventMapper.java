package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventRequest;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventResponse;
import UnitSystem.demo.DataAccessLayer.Entities.EventType;
import UnitSystem.demo.DataAccessLayer.Entities.UpcomingEvent;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpcomingEventMapper {

    private final UserRepository userRepository;

    public UpcomingEventResponse mapToResponse(UpcomingEvent event) {
        return UpcomingEventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .subtitle(event.getSubtitle())
                .eventDate(event.getEventDate())
                .type(event.getType().name())
                .userId(event.getUser().getId())
                .userName(event.getUser().getUserName())
                .createdAt(event.getCreatedAt())
                .build();
    }

    public UpcomingEvent mapToEntity(UpcomingEventRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        return UpcomingEvent.builder()
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .eventDate(request.getEventDate())
                .type(EventType.valueOf(request.getType()))
                .user(user)
                .build();
    }
}
