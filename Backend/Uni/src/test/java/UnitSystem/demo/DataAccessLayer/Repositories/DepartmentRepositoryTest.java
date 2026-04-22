package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department department;
    private Department department2;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Computer Science");

        department2 = new Department();
        department2.setName("Mathematics");
    }

    @Test
    void itShouldSaveDepartment() {
        Department savedDepartment = departmentRepository.save(department);

        assertNotNull(savedDepartment.getId());
        assertEquals(department.getName(), savedDepartment.getName());
    }

    @Test
    void itShouldSaveMultipleDepartments() {
        Department savedDepartment1 = departmentRepository.save(department);
        Department savedDepartment2 = departmentRepository.save(department2);

        assertNotNull(savedDepartment1.getId());
        assertNotNull(savedDepartment2.getId());
        assertEquals(2, departmentRepository.findAll().size());
    }

    @Test
    void itShouldFindDepartmentById() {
        Department savedDepartment = departmentRepository.save(department);

        Optional<Department> foundDepartment = departmentRepository.findById(savedDepartment.getId());

        assertTrue(foundDepartment.isPresent());
        assertEquals(savedDepartment.getId(), foundDepartment.get().getId());
        assertEquals(savedDepartment.getName(), foundDepartment.get().getName());
    }

    @Test
    void itShouldFindAllDepartments() {
        departmentRepository.save(department);
        departmentRepository.save(department2);

        List<Department> allDepartments = departmentRepository.findAll();

        assertEquals(2, allDepartments.size());
    }

    @Test
    void itShouldDeleteDepartment() {
        Department savedDepartment = departmentRepository.save(department);
        Long departmentId = savedDepartment.getId();

        departmentRepository.delete(savedDepartment);

        assertFalse(departmentRepository.existsById(departmentId));
    }

    @Test
    void itShouldDeleteDepartmentById() {
        Department savedDepartment = departmentRepository.save(department);
        Long departmentId = savedDepartment.getId();

        departmentRepository.deleteById(departmentId);

        assertFalse(departmentRepository.existsById(departmentId));
    }

    @Test
    void itShouldUpdateDepartment() {
        Department savedDepartment = departmentRepository.save(department);

        savedDepartment.setName("Updated Department Name");
        Department updatedDepartment = departmentRepository.save(savedDepartment);

        assertEquals("Updated Department Name", updatedDepartment.getName());
        assertEquals(savedDepartment.getId(), updatedDepartment.getId());
    }

    @Test
    void itShouldCheckIfDepartmentExistsById() {
        Department savedDepartment = departmentRepository.save(department);

        assertTrue(departmentRepository.existsById(savedDepartment.getId()));
        assertFalse(departmentRepository.existsById(999L));
    }

    @Test
    void itShouldCountDepartments() {
        assertEquals(0, departmentRepository.count());

        departmentRepository.save(department);
        assertEquals(1, departmentRepository.count());

        departmentRepository.save(department2);
        assertEquals(2, departmentRepository.count());
    }

    @Test
    void itShouldDeleteAllDepartments() {
        departmentRepository.save(department);
        departmentRepository.save(department2);
        assertEquals(2, departmentRepository.count());

        departmentRepository.deleteAll();

        assertEquals(0, departmentRepository.count());
    }

    @Test
    void itShouldReturnEmptyWhenDepartmentNotFound() {
        Optional<Department> foundDepartment = departmentRepository.findById(999L);

        assertFalse(foundDepartment.isPresent());
    }
}
