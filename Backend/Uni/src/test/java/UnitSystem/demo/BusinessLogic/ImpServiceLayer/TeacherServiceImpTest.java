package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.Mappers.MappingUtils;
import UnitSystem.demo.BusinessLogic.Mappers.TeacherMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Teacher.TeacherRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Teacher.TeacherResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Teacher;
import UnitSystem.demo.DataAccessLayer.Repositories.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceImpTest {

    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TeacherMapper teacherMapper;
    @Mock
    private MappingUtils mappingUtils;
    @InjectMocks
    private TeacherServiceImp teacherServiceImp;

    private Teacher teacher;
    private TeacherRequest teacherRequest;
    private TeacherResponse teacherResponse;

    @BeforeEach
    void setUp() {
        teacher = Teacher.builder()
                .id(1L)
                .userName("prof_smith")
                .email("smith@university.edu")
                .officeLocation("Building A, Room 101")
                .salary(new BigDecimal("75000"))
                .build();

        teacherRequest = TeacherRequest.builder()
                .userName("prof_smith")
                .email("smith@university.edu")
                .password("securePass")
                .active(true)
                .officeLocation("Building A, Room 101")
                .salary(new BigDecimal("75000"))
                .build();

        teacherResponse = TeacherResponse.builder()
                .id(1L)
                .userName("prof_smith")
                .email("smith@university.edu")
                .active(true)
                .officeLocation("Building A, Room 101")
                .salary(new BigDecimal("75000"))
                .build();
    }

    @Test
    void getAllTeachers_returnsListOfTeachers() {
        when(teacherRepository.findAll()).thenReturn(List.of(teacher));
        when(teacherMapper.mapToTeacherResponse(teacher)).thenReturn(teacherResponse);

        List<TeacherResponse> result = teacherServiceImp.getAllTeachers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("prof_smith", result.get(0).getUserName());
        verify(teacherRepository).findAll();
    }

    @Test
    void getTeacherById_existingId_returnsTeacherResponse() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherMapper.mapToTeacherResponse(teacher)).thenReturn(teacherResponse);

        TeacherResponse result = teacherServiceImp.getTeacherById(1L);

        assertNotNull(result);
        assertEquals("prof_smith", result.getUserName());
        verify(teacherRepository).findById(1L);
    }

    @Test
    void getTeacherById_nonExistingId_returnsNull() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        TeacherResponse result = teacherServiceImp.getTeacherById(99L);

        assertNull(result);
    }

    @Test
    void getTeacherByUserName_existingUserName_returnsTeacherResponse() {
        when(teacherRepository.findByUserName("prof_smith")).thenReturn(teacher);
        when(teacherMapper.mapToTeacherResponse(teacher)).thenReturn(teacherResponse);

        TeacherResponse result = teacherServiceImp.getTeacherByUserName("prof_smith");

        assertNotNull(result);
        assertEquals("prof_smith", result.getUserName());
    }

    @Test
    void getTeacherByUserName_nonExistingUserName_returnsNull() {
        when(teacherRepository.findByUserName("unknown")).thenReturn(null);

        TeacherResponse result = teacherServiceImp.getTeacherByUserName("unknown");

        assertNull(result);
    }

    @Test
    void createTeacher_validRequest_savesAndReturnsResponse() {
        when(teacherMapper.mapToTeacher(teacherRequest)).thenReturn(teacher);
        when(teacherMapper.mapToTeacherResponse(teacher)).thenReturn(teacherResponse);

        TeacherResponse result = teacherServiceImp.createTeacher(teacherRequest);

        assertNotNull(result);
        assertEquals("prof_smith", result.getUserName());
        verify(teacherRepository).save(teacher);
    }

    @Test
    void updateTeacher_existingId_updatesAndReturnsResponse() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(passwordEncoder.encode("securePass")).thenReturn("hashed_password");
        when(teacherMapper.mapToTeacherResponse(teacher)).thenReturn(teacherResponse);

        TeacherResponse result = teacherServiceImp.updateTeacher(1L, teacherRequest);

        assertNotNull(result);
        verify(teacherRepository).save(teacher);
        verify(passwordEncoder).encode("securePass");
    }

    @Test
    void updateTeacher_nonExistingId_throwsException() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> teacherServiceImp.updateTeacher(99L, teacherRequest));
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void deleteTeacher_callsRepositoryDeleteById() {
        doNothing().when(teacherRepository).deleteById(1L);

        teacherServiceImp.deleteTeacher(1L);

        verify(teacherRepository).deleteById(1L);
    }
}
