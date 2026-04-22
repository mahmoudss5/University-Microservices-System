package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TeacherRepositoryTest {

    @Autowired
    private TeacherRepository teacherRepository;

    private Teacher teacher;
    private Teacher teacher2;

    @BeforeEach
    void setUp() {
        teacher = new Teacher();
        teacher.setUserName("teacher1");
        teacher.setEmail("teacher1@gmail.com");
        teacher.setPassword("password");
        teacher.setActive(true);
        teacher.setOfficeLocation("Building A, Room 101");
        teacher.setSalary(BigDecimal.valueOf(5000));

        teacher2 = new Teacher();
        teacher2.setUserName("teacher2");
        teacher2.setEmail("teacher2@gmail.com");
        teacher2.setPassword("password");
        teacher2.setActive(true);
        teacher2.setOfficeLocation("Building B, Room 202");
        teacher2.setSalary(BigDecimal.valueOf(6000));
    }

    @Test
    void itShouldSaveTeacher() {
        Teacher savedTeacher = teacherRepository.save(teacher);

        assertNotNull(savedTeacher.getId());
        assertEquals(teacher.getUserName(), savedTeacher.getUserName());
        assertEquals(teacher.getEmail(), savedTeacher.getEmail());
        assertEquals(teacher.getOfficeLocation(), savedTeacher.getOfficeLocation());
        assertEquals(teacher.getSalary(), savedTeacher.getSalary());
    }

    @Test
    void itShouldSaveMultipleTeachers() {
        Teacher savedTeacher1 = teacherRepository.save(teacher);
        Teacher savedTeacher2 = teacherRepository.save(teacher2);

        assertNotNull(savedTeacher1.getId());
        assertNotNull(savedTeacher2.getId());
        assertEquals(2, teacherRepository.findAll().size());
    }

    @Test
    void itShouldFindTeacherById() {
        Teacher savedTeacher = teacherRepository.save(teacher);

        Optional<Teacher> foundTeacher = teacherRepository.findById(savedTeacher.getId());

        assertTrue(foundTeacher.isPresent());
        assertEquals(savedTeacher.getId(), foundTeacher.get().getId());
        assertEquals(savedTeacher.getUserName(), foundTeacher.get().getUserName());
    }

    @Test
    void itShouldFindAllTeachers() {
        teacherRepository.save(teacher);
        teacherRepository.save(teacher2);

        List<Teacher> allTeachers = teacherRepository.findAll();

        assertEquals(2, allTeachers.size());
    }

    @Test
    void itShouldDeleteTeacher() {
        Teacher savedTeacher = teacherRepository.save(teacher);
        Long teacherId = savedTeacher.getId();

        teacherRepository.delete(savedTeacher);

        assertFalse(teacherRepository.existsById(teacherId));
    }

    @Test
    void itShouldDeleteTeacherById() {
        Teacher savedTeacher = teacherRepository.save(teacher);
        Long teacherId = savedTeacher.getId();

        teacherRepository.deleteById(teacherId);

        assertFalse(teacherRepository.existsById(teacherId));
    }

    @Test
    void itShouldUpdateTeacher() {
        Teacher savedTeacher = teacherRepository.save(teacher);

        savedTeacher.setOfficeLocation("Building C, Room 303");
        savedTeacher.setSalary(BigDecimal.valueOf(7000));
        Teacher updatedTeacher = teacherRepository.save(savedTeacher);

        assertEquals("Building C, Room 303", updatedTeacher.getOfficeLocation());
        assertEquals(BigDecimal.valueOf(7000), updatedTeacher.getSalary());
        assertEquals(savedTeacher.getId(), updatedTeacher.getId());
    }

    @Test
    void itShouldCheckIfTeacherExistsById() {
        Teacher savedTeacher = teacherRepository.save(teacher);

        assertTrue(teacherRepository.existsById(savedTeacher.getId()));
        assertFalse(teacherRepository.existsById(999L));
    }

    @Test
    void itShouldCountTeachers() {
        assertEquals(0, teacherRepository.count());

        teacherRepository.save(teacher);
        assertEquals(1, teacherRepository.count());

        teacherRepository.save(teacher2);
        assertEquals(2, teacherRepository.count());
    }

    @Test
    void itShouldDeleteAllTeachers() {
        teacherRepository.save(teacher);
        teacherRepository.save(teacher2);
        assertEquals(2, teacherRepository.count());

        teacherRepository.deleteAll();

        assertEquals(0, teacherRepository.count());
    }

    @Test
    void itShouldReturnEmptyWhenTeacherNotFound() {
        Optional<Teacher> foundTeacher = teacherRepository.findById(999L);

        assertFalse(foundTeacher.isPresent());
    }
}
