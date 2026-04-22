package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Announcement;
import UnitSystem.demo.DataAccessLayer.Entities.Role;
import UnitSystem.demo.DataAccessLayer.Entities.UpcomingEvent;
import UnitSystem.demo.DataAccessLayer.Repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared mapping utilities used by both StudentMapper and TeacherMapper.
 * Centralises duplicated logic to adhere to the DRY principle.
 */
@Component
@RequiredArgsConstructor
public class MappingUtils {

    private final RoleRepository roleRepository;

    public Set<Role> mapRoleNamesToRoles(Set<String> roleNames) {
        return roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + name)))
                .collect(Collectors.toSet());
    }

    public AnnouncementResponse mapToAnnouncementResponse(Announcement announcement) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getDescription())
                .courseId(announcement.getCourse() != null ? announcement.getCourse().getId() : null)
                .createdDate(announcement.getCreatedAt())
                .build();
    }

    public UpcomingEventResponse mapToUpcomingEventResponse(UpcomingEvent event) {
        return UpcomingEventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .subtitle(event.getSubtitle())
                .eventDate(event.getEventDate())
                .type(event.getType().name())
                .userId(event.getUser() != null ? event.getUser().getId() : null)
                .userName(event.getUser() != null ? event.getUser().getUserName() : null)
                .createdAt(event.getCreatedAt())
                .build();
    }
}
