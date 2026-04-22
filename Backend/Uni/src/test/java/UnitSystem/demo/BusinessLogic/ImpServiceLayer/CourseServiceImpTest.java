package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.Mappers.CourseMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Entities.Student;
import UnitSystem.demo.DataAccessLayer.Entities.Teacher;
import UnitSystem.demo.DataAccessLayer.Repositories.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImpTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseMapper courseMapper;
    @InjectMocks
    private CourseServiceImp courseServiceImp;

    private Course csCourse;
    private Teacher teacher;
    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        teacher = Teacher.builder()
                .id(1L)
                .userName("teacher1")
                .email("teacher@gmail.com")
                .build();

        csCourse = Course.builder()
                .id(1L)
                .name("Computer Science 101")
                .courseCode("CS101")
                .description("Introduction to Computer Science")
                .credits(3)
                .capacity(30)
                .teacher(teacher)
                .build();

        student1 = Student.builder()
                .id(1L)
                .userName("student1")
                .email("student1@gmail.com")
                .build();

        student2 = Student.builder()
                .id(2L)
                .userName("student2")
                .email("student2@gmail.com")
                .build();
    }

    @Test
    void canGetCourseById() {
        CourseResponse expectedResponse = CourseResponse.builder()
                .id(1L)
                .name("Computer Science 101")
                .courseCode("CS101")
                .description("Introduction to Computer Science")
                .creditHours(3)
                .maxStudents(30)
                .teacherUserName("teacher1")
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(csCourse));
        when(courseMapper.mapToCourseResponse(csCourse)).thenReturn(expectedResponse);

        CourseResponse result = courseServiceImp.getCourseById(1L);

        assertNotNull(result);
        assertEquals("Computer Science 101", result.getName());
        verify(courseRepository).findById(1L);
        verify(courseMapper).mapToCourseResponse(csCourse);
    }
}
