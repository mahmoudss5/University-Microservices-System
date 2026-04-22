package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.CourseService;
import UnitSystem.demo.BusinessLogic.Mappers.AnnouncementMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Announcement;
import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Entities.Teacher;
import UnitSystem.demo.DataAccessLayer.Repositories.AnnouncementRepository;
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
class AnnouncementServiceImpTest {

    @Mock
    private AnnouncementRepository announcementRepository;
    @Mock
    private CourseService courseService;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private AnnouncementMapper announcementMapper;
    @InjectMocks
    private AnnouncementServiceImp announcementServiceImp;

    private Course course;
    private Announcement announcement;
    private AnnouncementRequest announcementRequest;
    private AnnouncementResponse announcementResponse;

    @BeforeEach
    void setUp() {
        Teacher teacher = Teacher.builder()
                .id(1L)
                .userName("prof_smith")
                .email("smith@university.edu")
                .build();

        course = Course.builder()
                .id(10L)
                .name("Computer Science 101")
                .courseCode("CS101")
                .teacher(teacher)
                .build();

        announcement = Announcement.builder()
                .id(1L)
                .course(course)
                .title("Midterm Reminder")
                .description("Midterm exam is next week.")
                .build();

        announcementRequest = AnnouncementRequest.builder()
                .title("Midterm Reminder")
                .content("Midterm exam is next week.")
                .courseId(10L)
                .build();

        announcementResponse = AnnouncementResponse.builder()
                .id(1L)
                .title("Midterm Reminder")
                .content("Midterm exam is next week.")
                .courseId(10L)
                .createdDate(LocalDateTime.now())
                .build();
    }

    @Test
    void createAnnouncement_validRequest_savesAnnouncementAndNotifies() {
        when(courseService.getCourseEntityById(10L)).thenReturn(course);
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(courseService.findStudentEmailsByCourseId(10L)).thenReturn(List.of("student@example.com"));
        when(announcementMapper.mapToResponse(any(Announcement.class))).thenReturn(announcementResponse);

        announcementServiceImp.createAnnouncement(announcementRequest);

        verify(announcementRepository).save(any(Announcement.class));
        verify(courseService).findStudentEmailsByCourseId(10L);
        verify(simpMessagingTemplate).convertAndSendToUser(
                eq("student@example.com"), eq("/queue/announcements"), any());
    }

    @Test
    void createAnnouncement_noEnrolledStudents_savesWithoutNotifying() {
        when(courseService.getCourseEntityById(10L)).thenReturn(course);
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(courseService.findStudentEmailsByCourseId(10L)).thenReturn(List.of());
        when(announcementMapper.mapToResponse(any(Announcement.class))).thenReturn(announcementResponse);

        announcementServiceImp.createAnnouncement(announcementRequest);

        verify(announcementRepository).save(any(Announcement.class));
        verify(simpMessagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    void deleteAnnouncement_existingId_deletesAnnouncement() {
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(announcement));

        announcementServiceImp.deleteAnnouncement(1L);

        verify(announcementRepository).delete(announcement);
    }

    @Test
    void deleteAnnouncement_nonExistingId_throwsException() {
        when(announcementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> announcementServiceImp.deleteAnnouncement(99L));
        verify(announcementRepository, never()).delete(any());
    }

    @Test
    void getAnnouncementById_existingId_returnsResponse() {
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(announcement));
        when(announcementMapper.mapToResponse(announcement)).thenReturn(announcementResponse);

        AnnouncementResponse result = announcementServiceImp.getAnnouncementById(1L);

        assertNotNull(result);
        assertEquals("Midterm Reminder", result.getTitle());
    }

    @Test
    void getAnnouncementById_nonExistingId_throwsException() {
        when(announcementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> announcementServiceImp.getAnnouncementById(99L));
    }

    @Test
    void getAnnouncementsByCourseId_returnsListOfResponses() {
        when(announcementRepository.findByCourseId(10L)).thenReturn(List.of(announcement));
        when(announcementMapper.mapToResponse(announcement)).thenReturn(announcementResponse);

        List<AnnouncementResponse> result = announcementServiceImp.getAnnouncementsByCourseId(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Midterm Reminder", result.get(0).getTitle());
        verify(announcementRepository).findByCourseId(10L);
    }

    @Test
    void getAllAnnouncements_returnsListOfAllAnnouncements() {
        when(announcementRepository.findAll()).thenReturn(List.of(announcement));
        when(announcementMapper.mapToResponse(announcement)).thenReturn(announcementResponse);

        List<AnnouncementResponse> result = announcementServiceImp.getAllAnnouncements();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(announcementRepository).findAll();
    }
}
