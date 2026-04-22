package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;

import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;

import java.util.List;

public interface AnnouncementService {
        void createAnnouncement(AnnouncementRequest request);
        void deleteAnnouncement(Long id);
        AnnouncementResponse getAnnouncementById(Long id);
        List<AnnouncementResponse> getAnnouncementsByCourseId(Long courseId);
        List<AnnouncementResponse> getAllAnnouncements();
}
