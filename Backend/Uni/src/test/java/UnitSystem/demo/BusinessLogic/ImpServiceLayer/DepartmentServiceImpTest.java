package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.Mappers.DepartmentMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentResponse;
import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentsDetails;
import UnitSystem.demo.DataAccessLayer.Entities.Department;
import UnitSystem.demo.DataAccessLayer.Repositories.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImpTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private DepartmentMapper departmentMapper;
    @InjectMocks
    private DepartmentServiceImp departmentServiceImp;

    private Department department;
    private DepartmentRequest departmentRequest;
    private DepartmentResponse departmentResponse;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Computer Science")
                .build();

        departmentRequest = DepartmentRequest.builder()
                .name("Computer Science")
                .build();

        departmentResponse = DepartmentResponse.builder()
                .id(1L)
                .name("Computer Science")
                .build();
    }

    @Test
    void getAllDepartments_returnsListFromRepository() {
        DepartmentsDetails details = mock(DepartmentsDetails.class);
        when(departmentRepository.findAllDepartmentsWithCourseCount()).thenReturn(List.of(details));

        List<DepartmentsDetails> result = departmentServiceImp.getAllDepartments();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(departmentRepository).findAllDepartmentsWithCourseCount();
    }

    @Test
    void getDepartmentById_existingId_returnsDepartmentResponse() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentMapper.mapToDepartmentResponse(department)).thenReturn(departmentResponse);

        DepartmentResponse result = departmentServiceImp.getDepartmentById(1L);

        assertNotNull(result);
        assertEquals("Computer Science", result.getName());
        verify(departmentRepository).findById(1L);
    }

    @Test
    void getDepartmentById_nonExistingId_returnsNull() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        DepartmentResponse result = departmentServiceImp.getDepartmentById(99L);

        assertNull(result);
    }

    @Test
    void getDepartmentByName_existingName_returnsDepartmentResponse() {
        when(departmentRepository.findByName("Computer Science")).thenReturn(department);
        when(departmentMapper.mapToDepartmentResponse(department)).thenReturn(departmentResponse);

        DepartmentResponse result = departmentServiceImp.getDepartmentByName("Computer Science");

        assertNotNull(result);
        assertEquals("Computer Science", result.getName());
    }

    @Test
    void getDepartmentByName_nonExistingName_returnsNull() {
        when(departmentRepository.findByName("Unknown")).thenReturn(null);

        DepartmentResponse result = departmentServiceImp.getDepartmentByName("Unknown");

        assertNull(result);
    }

    @Test
    void createDepartment_validRequest_savesAndReturnsResponse() {
        when(departmentMapper.mapToDepartment(departmentRequest)).thenReturn(department);
        when(departmentMapper.mapToDepartmentResponse(department)).thenReturn(departmentResponse);

        DepartmentResponse result = departmentServiceImp.createDepartment(departmentRequest);

        assertNotNull(result);
        assertEquals("Computer Science", result.getName());
        verify(departmentRepository).save(department);
    }

    @Test
    void updateDepartment_existingId_updatesAndReturnsResponse() {
        DepartmentRequest updateRequest = DepartmentRequest.builder().name("Advanced CS").build();
        DepartmentResponse updatedResponse = DepartmentResponse.builder().id(1L).name("Advanced CS").build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentMapper.mapToDepartmentResponse(department)).thenReturn(updatedResponse);

        DepartmentResponse result = departmentServiceImp.updateDepartment(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Advanced CS", result.getName());
        verify(departmentRepository).save(department);
    }

    @Test
    void updateDepartment_nonExistingId_throwsException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> departmentServiceImp.updateDepartment(99L, departmentRequest));
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void deleteDepartment_callsRepositoryDeleteById() {
        doNothing().when(departmentRepository).deleteById(1L);

        departmentServiceImp.deleteDepartment(1L);

        verify(departmentRepository).deleteById(1L);
    }

    @Test
    void existsByName_existingName_returnsTrue() {
        when(departmentRepository.existsByName("Computer Science")).thenReturn(true);

        Boolean result = departmentServiceImp.existsByName("Computer Science");

        assertTrue(result);
    }

    @Test
    void existsByName_nonExistingName_returnsFalse() {
        when(departmentRepository.existsByName("Unknown")).thenReturn(false);

        Boolean result = departmentServiceImp.existsByName("Unknown");

        assertFalse(result);
    }
}
