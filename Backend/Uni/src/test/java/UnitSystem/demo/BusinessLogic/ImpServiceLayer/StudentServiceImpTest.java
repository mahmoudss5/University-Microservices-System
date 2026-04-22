package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.Mappers.MappingUtils;
import UnitSystem.demo.BusinessLogic.Mappers.StudentMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Student.StudentRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Student.StudentResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Student;
import UnitSystem.demo.DataAccessLayer.Repositories.StudentRepository;
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
class StudentServiceImpTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private MappingUtils mappingUtils;
    @InjectMocks
    private StudentServiceImp studentServiceImp;

    private Student student;
    private StudentRequest studentRequest;
    private StudentResponse studentResponse;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(1L)
                .userName("john_doe")
                .email("john@example.com")
                .gpa(new BigDecimal("3.8"))
                .enrollmentYear(2022)
                .totalCredits(60)
                .build();

        studentRequest = StudentRequest.builder()
                .userName("john_doe")
                .email("john@example.com")
                .password("password123")
                .active(true)
                .gpa(new BigDecimal("3.8"))
                .enrollmentYear(2022)
                .totalCredits(60)
                .build();

        studentResponse = StudentResponse.builder()
                .id(1L)
                .userName("john_doe")
                .email("john@example.com")
                .active(true)
                .gpa(new BigDecimal("3.8"))
                .enrollmentYear(2022)
                .totalCredits(60)
                .build();
    }

    @Test
    void getAllStudents_returnsListOfStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(studentMapper.mapToStudentResponse(student)).thenReturn(studentResponse);

        List<StudentResponse> result = studentServiceImp.getAllStudents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("john_doe", result.get(0).getUserName());
        verify(studentRepository).findAll();
    }

    @Test
    void getStudentById_existingId_returnsStudentResponse() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentMapper.mapToStudentResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentServiceImp.getStudentById(1L);

        assertNotNull(result);
        assertEquals("john_doe", result.getUserName());
        verify(studentRepository).findById(1L);
    }

    @Test
    void getStudentById_nonExistingId_returnsNull() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        StudentResponse result = studentServiceImp.getStudentById(99L);

        assertNull(result);
    }

    @Test
    void getStudentByUserName_existingUserName_returnsStudentResponse() {
        when(studentRepository.findByUserName("john_doe")).thenReturn(student);
        when(studentMapper.mapToStudentResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentServiceImp.getStudentByUserName("john_doe");

        assertNotNull(result);
        assertEquals("john_doe", result.getUserName());
    }

    @Test
    void getStudentByUserName_nonExistingUserName_returnsNull() {
        when(studentRepository.findByUserName("unknown")).thenReturn(null);

        StudentResponse result = studentServiceImp.getStudentByUserName("unknown");

        assertNull(result);
    }

    @Test
    void createStudent_validRequest_savesAndReturnsResponse() {
        when(studentMapper.mapToStudent(studentRequest)).thenReturn(student);
        when(studentMapper.mapToStudentResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentServiceImp.createStudent(studentRequest);

        assertNotNull(result);
        assertEquals("john_doe", result.getUserName());
        verify(studentRepository).save(student);
    }

    @Test
    void updateStudent_existingId_updatesAndReturnsResponse() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(studentMapper.mapToStudentResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentServiceImp.updateStudent(1L, studentRequest);

        assertNotNull(result);
        verify(studentRepository).save(student);
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void updateStudent_nonExistingId_throwsException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> studentServiceImp.updateStudent(99L, studentRequest));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void deleteStudent_callsRepositoryDeleteById() {
        doNothing().when(studentRepository).deleteById(1L);

        studentServiceImp.deleteStudent(1L);

        verify(studentRepository).deleteById(1L);
    }
}
