package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.CourseService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.UserService;
import UnitSystem.demo.BusinessLogic.Mappers.EnrolledCoursesMapper;
import UnitSystem.demo.DataAccessLayer.Dto.EnrolledCourse.EnrolledCourseRequest;
import UnitSystem.demo.DataAccessLayer.Dto.EnrolledCourse.EnrolledCourseResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Entities.EnrolledCourse;
import UnitSystem.demo.DataAccessLayer.Entities.Student;
import UnitSystem.demo.DataAccessLayer.Entities.Teacher;
import UnitSystem.demo.DataAccessLayer.Repositories.EnrolledCourseRepository;
import UnitSystem.demo.ExcHandler.Entites.ResourceNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrolledCourseServiceImpTest {

    @Mock
    private EnrolledCourseRepository enrolledCourseRepository;
    @Mock
    private UserService userService;
    @Mock
    private CourseService courseService;
    @Mock
    private EnrolledCoursesMapper mapper;
    @InjectMocks
    private EnrolledCourseServiceImp enrolledCourseServiceImp;

    private Student student;
    private Teacher teacher;
    private Course course;
    private EnrolledCourse enrolledCourse;
    private EnrolledCourseRequest enrolledCourseRequest;
    private EnrolledCourseResponse enrolledCourseResponse;

    @BeforeEach
    void setUp() {
        teacher = Teacher.builder()
                .id(2L)
                .userName("prof_smith")
                .email("smith@university.edu")
                .build();

        student = Student.builder()
                .id(1L)
                .userName("john_doe")
                .email("john@example.com")
                .build();

        course = Course.builder()
                .id(10L)
                .name("Computer Science 101")
                .courseCode("CS101")
                .teacher(teacher)
                .build();

        enrolledCourse = EnrolledCourse.builder()
                .id(100L)
                .student(student)
                .course(course)
                .enrollmentDate(LocalDateTime.now())
                .build();

        enrolledCourseRequest = EnrolledCourseRequest.builder()
                .studentId(1L)
                .courseId(10L)
                .build();

        enrolledCourseResponse = EnrolledCourseResponse.builder()
                .id(100L)
                .studentId(1L)
                .studentName("john_doe")
                .courseId(10L)
                .courseName("Computer Science 101")
                .build();
    }

    @Test
    void getAllEnrolledCourses_returnsListOfEnrolledCourses() {
        when(enrolledCourseRepository.findAll()).thenReturn(List.of(enrolledCourse));
        when(mapper.mapToEnrolledCourseResponse(enrolledCourse)).thenReturn(enrolledCourseResponse);

        List<EnrolledCourseResponse> result = enrolledCourseServiceImp.getAllEnrolledCourses();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(enrolledCourseRepository).findAll();
    }

    @Test
    void getEnrolledCoursesByStudentId_existingStudent_returnsEnrollments() {
        when(userService.findUserById(1L)).thenReturn(student);
        when(enrolledCourseRepository.findByStudent(student)).thenReturn(List.of(enrolledCourse));
        when(mapper.mapToEnrolledCourseResponse(enrolledCourse)).thenReturn(enrolledCourseResponse);

        List<EnrolledCourseResponse> result = enrolledCourseServiceImp.getEnrolledCoursesByStudentId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userService).findUserById(1L);
    }

    @Test
    void getEnrolledCoursesByCourseId_existingCourse_returnsEnrollments() {
        when(courseService.getCourseEntityById(10L)).thenReturn(course);
        when(enrolledCourseRepository.findByCourse(course)).thenReturn(List.of(enrolledCourse));
        when(mapper.mapToEnrolledCourseResponse(enrolledCourse)).thenReturn(enrolledCourseResponse);

        List<EnrolledCourseResponse> result = enrolledCourseServiceImp.getEnrolledCoursesByCourseId(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(courseService).getCourseEntityById(10L);
    }

    @Test
    void getEnrolledCourseById_existingId_returnsResponse() {
        when(enrolledCourseRepository.findById(100L)).thenReturn(Optional.of(enrolledCourse));
        when(mapper.mapToEnrolledCourseResponse(enrolledCourse)).thenReturn(enrolledCourseResponse);

        EnrolledCourseResponse result = enrolledCourseServiceImp.getEnrolledCourseById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    void getEnrolledCourseById_nonExistingId_throwsException() {
        when(enrolledCourseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> enrolledCourseServiceImp.getEnrolledCourseById(999L));
    }

    @Test
    void enrollStudentInCourse_newEnrollment_savesAndReturnsResponse() {
        when(userService.findUserById(1L)).thenReturn(student);
        when(courseService.getCourseEntityById(10L)).thenReturn(course);
        when(enrolledCourseRepository.existsByStudentAndCourse(student, course)).thenReturn(false);
        when(mapper.mapToEnrolledCourse(enrolledCourseRequest)).thenReturn(enrolledCourse);
        when(mapper.mapToEnrolledCourseResponse(enrolledCourse)).thenReturn(enrolledCourseResponse);

        EnrolledCourseResponse result = enrolledCourseServiceImp.enrollStudentInCourse(enrolledCourseRequest);

        assertNotNull(result);
        verify(enrolledCourseRepository).save(enrolledCourse);
    }

    @Test
    void enrollStudentInCourse_alreadyEnrolled_throwsException() {
        when(userService.findUserById(1L)).thenReturn(student);
        when(courseService.getCourseEntityById(10L)).thenReturn(course);
        when(enrolledCourseRepository.existsByStudentAndCourse(student, course)).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> enrolledCourseServiceImp.enrollStudentInCourse(enrolledCourseRequest));
        verify(enrolledCourseRepository, never()).save(any());
    }

    @Test
    void unenrollStudentFromCourse_callsRepositoryDeleteById() {
        doNothing().when(enrolledCourseRepository).deleteById(100L);

        enrolledCourseServiceImp.unenrollStudentFromCourse(100L);

        verify(enrolledCourseRepository).deleteById(100L);
    }

    @Test
    void isStudentEnrolledInCourse_enrolledStudent_returnsTrue() {
        when(userService.findUserById(1L)).thenReturn(student);
        when(courseService.getCourseEntityById(10L)).thenReturn(course);
        when(enrolledCourseRepository.existsByStudentAndCourse(student, course)).thenReturn(true);

        boolean result = enrolledCourseServiceImp.isStudentEnrolledInCourse(1L, 10L);

        assertTrue(result);
    }

    @Test
    void isStudentEnrolledInCourse_notEnrolledStudent_returnsFalse() {
        when(userService.findUserById(1L)).thenReturn(student);
        when(courseService.getCourseEntityById(10L)).thenReturn(course);
        when(enrolledCourseRepository.existsByStudentAndCourse(student, course)).thenReturn(false);

        boolean result = enrolledCourseServiceImp.isStudentEnrolledInCourse(1L, 10L);

        assertFalse(result);
    }
}
