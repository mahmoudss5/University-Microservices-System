package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Announcement;
import org.springframework.stereotype.Component;

@Component
public class AnnouncementMapper {

    public AnnouncementResponse mapToResponse(Announcement announcement) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getDescription())
                .courseId(announcement.getCourse().getId())
                .createdDate(announcement.getCreatedAt())
                .build();
    }
}
