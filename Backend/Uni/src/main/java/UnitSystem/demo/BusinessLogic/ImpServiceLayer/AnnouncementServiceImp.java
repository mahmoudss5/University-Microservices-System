package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.Aspect.Security.TeachersOnly;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.AnnouncementService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.CourseService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.EnrolledCourseService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.StudentService;
import UnitSystem.demo.BusinessLogic.Mappers.AnnouncementMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;
import UnitSystem.demo.DataAccessLayer.Dto.EnrolledCourse.EnrolledCourseResponse;
import UnitSystem.demo.DataAccessLayer.Dto.Student.StudentResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Announcement;
import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Repositories.AnnouncementRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImp implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final CourseService courseService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final AnnouncementMapper announcementMapper;

 

    @Override
    @TeachersOnly
    @CacheEvict(value = "announcementsCache", allEntries = true)
    public void createAnnouncement(AnnouncementRequest request) {
        Course course = courseService.getCourseEntityById(request.getCourseId());
        Announcement announcement = Announcement.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getContent())
                .build();
        announcementRepository.save(announcement);
        sendAnnouncementToCourseUsers(announcement);
    }

    @Override
    @TeachersOnly
    @CacheEvict(value = "announcementsCache", allEntries = true)
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found"));
        announcementRepository.delete(announcement);
    }

    @Override
    @Cacheable(value = "announcementsCache", key = "'announcementById:' + #id")
    public AnnouncementResponse getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found"));
        return announcementMapper.mapToResponse(announcement);
    }

    @Override
    @Cacheable(value = "announcementsCache", key = "'announcementsByCourse:' + #courseId")
    public List<AnnouncementResponse> getAnnouncementsByCourseId(Long courseId) {
        List<Announcement> announcements = announcementRepository.findByCourseId(courseId);
        return announcements.stream()
                .map(announcementMapper::mapToResponse)
                .collect(toList());
    }


    public void sendAnnouncementToCourseUsers(Announcement announcement) {
        Long courseId = announcement.getCourse().getId();
        String destination = "/queue/announcements";
        List<String> emails = courseService.findStudentEmailsByCourseId(courseId);
        for (String email : emails) {
            simpMessagingTemplate.convertAndSendToUser(email, destination, announcementMapper.mapToResponse(announcement));
        }
    }


    @Override
    @Cacheable(value = "announcementsCache", key = "'allAnnouncements'")
    public List<AnnouncementResponse> getAllAnnouncements() {
        List<Announcement> announcements = announcementRepository.findAll();
        return announcements.stream()
                .map(announcementMapper::mapToResponse)
                .collect(toList());
    }


}
