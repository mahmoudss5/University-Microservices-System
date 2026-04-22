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
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrolledCourseRepository enrolledCourseRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    private Student savedStudent;
    private Course savedCourse;
    private Course savedCourse2;
    private EnrolledCourse savedEnrolledCourse;
    private EnrolledCourse savedEnrolledCourse2;
    private Teacher savedTeacher;
    private Department savedDepartment;

    @BeforeEach
    void setUp() {
        // Create and save department first
        Department department = new Department();
        department.setName("Computer Science");
        savedDepartment = departmentRepository.save(department);

        // Create and save teacher
        Teacher teacher = new Teacher();
        teacher.setUserName("Dr. Smith");
        teacher.setEmail("drsmith@gmail.com");
        teacher.setPassword("password");
        teacher.setActive(true);
        teacher.setOfficeLocation("Building A");
        teacher.setSalary(BigDecimal.valueOf(5000));
        savedTeacher = teacherRepository.save(teacher);

        // Create and save courses
        Course course = new Course();
        course.setName("Math 101");
        course.setDepartment(savedDepartment);
        course.setCredits(3);
        course.setCapacity(30);
        course.setTeacher(savedTeacher);
        savedCourse = courseRepository.save(course);

        Course course2 = new Course();
        course2.setName("Physics 101");
        course2.setDepartment(savedDepartment);
        course2.setCredits(4);
        course2.setCapacity(25);
        course2.setTeacher(savedTeacher);
        savedCourse2 = courseRepository.save(course2);

        // Create and save student with all required fields
        Student student = new Student();
        student.setUserName("John Doe");
        student.setEmail("johndoe@gmail.com");
        student.setPassword("johndoe");
        student.setActive(true);
        student.setGpa(BigDecimal.valueOf(3.5));
        student.setEnrollmentYear(2024);
        student.setTotalCredits(30);
        savedStudent = studentRepository.save(student);

        // Create and save enrolled courses
        EnrolledCourse enrolledCourse = new EnrolledCourse();
        enrolledCourse.setStudent(savedStudent);
        enrolledCourse.setCourse(savedCourse);
        enrolledCourse.setEnrollmentDate(LocalDateTime.now());
        savedEnrolledCourse = enrolledCourseRepository.save(enrolledCourse);

        EnrolledCourse enrolledCourse2 = new EnrolledCourse();
        enrolledCourse2.setStudent(savedStudent);
        enrolledCourse2.setCourse(savedCourse2);
        enrolledCourse2.setEnrollmentDate(LocalDateTime.now());
        savedEnrolledCourse2 = enrolledCourseRepository.save(enrolledCourse2);
    }

    @Test
    void testFindStudentById() {
        Student foundStudent = studentRepository.findById(savedStudent.getId()).orElse(null);
        assertNotNull(foundStudent);
        assertEquals(savedStudent.getUserName(), foundStudent.getUserName());
    }

    @Test
    void testFindCoursesByStudentId() {
        List<Course> courses = studentRepository.findCoursesByStudentId(savedStudent.getId());
        assertNotNull(courses);
        assertEquals(2, courses.size());
        assertTrue(courses.stream().anyMatch(c -> c.getName().equals(savedCourse.getName())));
        assertTrue(courses.stream().anyMatch(c -> c.getName().equals(savedCourse2.getName())));
    }

    @Test
    void testSaveStudent() {
        Student newStudent = new Student();
        newStudent.setUserName("Jane Doe");
        newStudent.setEmail("janedoe@gmail.com");
        newStudent.setPassword("password");
        newStudent.setActive(true);
        newStudent.setGpa(BigDecimal.valueOf(3.8));
        newStudent.setEnrollmentYear(2024);
        newStudent.setTotalCredits(0);
        Student saved = studentRepository.save(newStudent);
        assertNotNull(saved.getId());
        assertEquals("Jane Doe", saved.getUserName());
    }

    @Test
    void testDeleteStudent() {
        studentRepository.deleteById(savedStudent.getId());
        Optional<Student> deletedStudent = studentRepository.findById(savedStudent.getId());
        assertFalse(deletedStudent.isPresent());
    }
}