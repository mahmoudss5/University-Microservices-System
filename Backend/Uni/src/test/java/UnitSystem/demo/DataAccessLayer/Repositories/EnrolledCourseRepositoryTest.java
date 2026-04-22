package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EnrolledCourseRepositoryTest {

    @Autowired
    private EnrolledCourseRepository enrolledCourseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    private EnrolledCourse enrolledCourse;
    private EnrolledCourse enrolledCourse2;
    private Student student;
    private Student student2;
    private Course course;
    private Course course2;
    private Department department;
    private Teacher teacher;

    @BeforeEach
    void setUp() {
        // Create and save department
        department = new Department();
        department.setName("Computer Science");
        department = departmentRepository.save(department);

        // Create and save teacher
        teacher = new Teacher();
        teacher.setUserName("teacher1");
        teacher.setEmail("teacher1@gmail.com");
        teacher.setPassword("password");
        teacher.setActive(true);
        teacher.setOfficeLocation("Building A");
        teacher.setSalary(BigDecimal.valueOf(5000));
        teacher = teacherRepository.save(teacher);

        // Create and save courses
        course = new Course();
        course.setName("Data Structures");
        course.setDepartment(department);
        course.setCredits(3);
        course.setCapacity(30);
        course.setTeacher(teacher);
        course = courseRepository.save(course);

        course2 = new Course();
        course2.setName("Algorithms");
        course2.setDepartment(department);
        course2.setCredits(4);
        course2.setCapacity(25);
        course2.setTeacher(teacher);
        course2 = courseRepository.save(course2);

        // Create and save students
        student = new Student();
        student.setUserName("student1");
        student.setEmail("student1@gmail.com");
        student.setPassword("password");
        student.setActive(true);
        student.setGpa(BigDecimal.valueOf(3.5));
        student.setEnrollmentYear(2024);
        student.setTotalCredits(30);
        student = studentRepository.save(student);

        student2 = new Student();
        student2.setUserName("student2");
        student2.setEmail("student2@gmail.com");
        student2.setPassword("password");
        student2.setActive(true);
        student2.setGpa(BigDecimal.valueOf(3.8));
        student2.setEnrollmentYear(2024);
        student2.setTotalCredits(25);
        student2 = studentRepository.save(student2);

        // Create enrolled courses (don't save yet)
        enrolledCourse = new EnrolledCourse();
        enrolledCourse.setStudent(student);
        enrolledCourse.setCourse(course);
        enrolledCourse.setEnrollmentDate(LocalDateTime.now());

        enrolledCourse2 = new EnrolledCourse();
        enrolledCourse2.setStudent(student2);
        enrolledCourse2.setCourse(course2);
        enrolledCourse2.setEnrollmentDate(LocalDateTime.now());
    }

    @Test
    void itShouldSaveEnrolledCourse() {
        EnrolledCourse savedEnrolledCourse = enrolledCourseRepository.save(enrolledCourse);

        assertNotNull(savedEnrolledCourse.getId());
        assertEquals(enrolledCourse.getStudent().getId(), savedEnrolledCourse.getStudent().getId());
        assertEquals(enrolledCourse.getCourse().getId(), savedEnrolledCourse.getCourse().getId());
        assertNotNull(savedEnrolledCourse.getEnrollmentDate());
    }

    @Test
    void itShouldSaveMultipleEnrolledCourses() {
        EnrolledCourse savedEnrolledCourse1 = enrolledCourseRepository.save(enrolledCourse);
        EnrolledCourse savedEnrolledCourse2 = enrolledCourseRepository.save(enrolledCourse2);

        assertNotNull(savedEnrolledCourse1.getId());
        assertNotNull(savedEnrolledCourse2.getId());
        assertEquals(2, enrolledCourseRepository.findAll().size());
    }

    @Test
    void itShouldFindEnrolledCourseById() {
        EnrolledCourse savedEnrolledCourse = enrolledCourseRepository.save(enrolledCourse);

        Optional<EnrolledCourse> foundEnrolledCourse = enrolledCourseRepository.findById(savedEnrolledCourse.getId());

        assertTrue(foundEnrolledCourse.isPresent());
        assertEquals(savedEnrolledCourse.getId(), foundEnrolledCourse.get().getId());
    }

    @Test
    void itShouldFindAllEnrolledCourses() {
        enrolledCourseRepository.save(enrolledCourse);
        enrolledCourseRepository.save(enrolledCourse2);

        List<EnrolledCourse> allEnrolledCourses = enrolledCourseRepository.findAll();

        assertEquals(2, allEnrolledCourses.size());
    }

    @Test
    void itShouldDeleteEnrolledCourse() {
        EnrolledCourse savedEnrolledCourse = enrolledCourseRepository.save(enrolledCourse);
        Long enrolledCourseId = savedEnrolledCourse.getId();

        enrolledCourseRepository.delete(savedEnrolledCourse);

        assertFalse(enrolledCourseRepository.existsById(enrolledCourseId));
    }

    @Test
    void itShouldDeleteEnrolledCourseById() {
        EnrolledCourse savedEnrolledCourse = enrolledCourseRepository.save(enrolledCourse);
        Long enrolledCourseId = savedEnrolledCourse.getId();

        enrolledCourseRepository.deleteById(enrolledCourseId);

        assertFalse(enrolledCourseRepository.existsById(enrolledCourseId));
    }

    @Test
    void itShouldUpdateEnrolledCourse() {
        EnrolledCourse savedEnrolledCourse = enrolledCourseRepository.save(enrolledCourse);
        LocalDateTime newDate = LocalDateTime.now().plusDays(1);

        savedEnrolledCourse.setEnrollmentDate(newDate);
        EnrolledCourse updatedEnrolledCourse = enrolledCourseRepository.save(savedEnrolledCourse);

        assertEquals(newDate, updatedEnrolledCourse.getEnrollmentDate());
        assertEquals(savedEnrolledCourse.getId(), updatedEnrolledCourse.getId());
    }

    @Test
    void itShouldCheckIfEnrolledCourseExistsById() {
        EnrolledCourse savedEnrolledCourse = enrolledCourseRepository.save(enrolledCourse);

        assertTrue(enrolledCourseRepository.existsById(savedEnrolledCourse.getId()));
        assertFalse(enrolledCourseRepository.existsById(999L));
    }

    @Test
    void itShouldCountEnrolledCourses() {
        assertEquals(0, enrolledCourseRepository.count());

        enrolledCourseRepository.save(enrolledCourse);
        assertEquals(1, enrolledCourseRepository.count());

        enrolledCourseRepository.save(enrolledCourse2);
        assertEquals(2, enrolledCourseRepository.count());
    }

    @Test
    void itShouldDeleteAllEnrolledCourses() {
        enrolledCourseRepository.save(enrolledCourse);
        enrolledCourseRepository.save(enrolledCourse2);
        assertEquals(2, enrolledCourseRepository.count());

        enrolledCourseRepository.deleteAll();

        assertEquals(0, enrolledCourseRepository.count());
    }



    @Test
    void itShouldAllowMultipleStudentsInSameCourse() {
        // Enroll both students in the same course
        EnrolledCourse enrollment1 = new EnrolledCourse();
        enrollment1.setStudent(student);
        enrollment1.setCourse(course);
        enrollment1.setEnrollmentDate(LocalDateTime.now());
        enrolledCourseRepository.save(enrollment1);

        EnrolledCourse enrollment2 = new EnrolledCourse();
        enrollment2.setStudent(student2);
        enrollment2.setCourse(course);
        enrollment2.setEnrollmentDate(LocalDateTime.now());
        enrolledCourseRepository.save(enrollment2);

        List<EnrolledCourse> enrollments = enrolledCourseRepository.findAll();
        assertEquals(2, enrollments.size());
    }

    @Test
    void itShouldAllowSameStudentInMultipleCourses() {
        // Enroll same student in both courses
        EnrolledCourse enrollment1 = new EnrolledCourse();
        enrollment1.setStudent(student);
        enrollment1.setCourse(course);
        enrollment1.setEnrollmentDate(LocalDateTime.now());
        enrolledCourseRepository.save(enrollment1);

        EnrolledCourse enrollment2 = new EnrolledCourse();
        enrollment2.setStudent(student);
        enrollment2.setCourse(course2);
        enrollment2.setEnrollmentDate(LocalDateTime.now());
        enrolledCourseRepository.save(enrollment2);

        List<EnrolledCourse> enrollments = enrolledCourseRepository.findAll();
        assertEquals(2, enrollments.size());
    }
}
