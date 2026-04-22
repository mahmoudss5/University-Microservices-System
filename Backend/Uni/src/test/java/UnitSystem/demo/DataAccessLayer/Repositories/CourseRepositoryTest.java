package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private EnrolledCourseRepository enrolledCourseRepository;

    @Autowired
    private StudentRepository studentRepository;

    private Course course;
    private Course course2;
    private Department department;
    private Department department2;
    private Teacher teacher;
    private Teacher teacher2;

    @BeforeEach
    void setUp() {
        // Create and save department
        department = new Department();
        department.setName(DepartmentsType.Computer_Science.name());
        department = departmentRepository.save(department);

        department2 = new Department();
        department2.setName(DepartmentsType.Information_Technology.name());
        department2 = departmentRepository.save(department2);

        // Create and save teacher
        teacher = new Teacher();
        teacher.setUserName("teacher1");
        teacher.setEmail("teacher1@gmail.com");
        teacher.setPassword("password");
        teacher.setActive(true);
        teacher.setOfficeLocation("Building A");
        teacher.setSalary(BigDecimal.valueOf(5000));
        teacher = teacherRepository.save(teacher);

        teacher2 = new Teacher();
        teacher2.setUserName("teacher2");
        teacher2.setEmail("teacher2@gmail.com");
        teacher2.setPassword("password");
        teacher2.setActive(true);
        teacher2.setOfficeLocation("Building B");
        teacher2.setSalary(BigDecimal.valueOf(6000));
        teacher2 = teacherRepository.save(teacher2);

        // Create courses (don't save yet - will be done in tests)
        course = new Course();
        course.setName("Data Structures");
        course.setDepartment(department);
        course.setCredits(3);
        course.setCapacity(30);
        course.setTeacher(teacher);

        course2 = new Course();
        course2.setName("Algorithms");
        course2.setDepartment(department);
        course2.setCredits(4);
        course2.setCapacity(25);
        course2.setTeacher(teacher2);
    }

    @Test
    void itShouldSaveCourse() {
        Course savedCourse = courseRepository.save(course);

        assertNotNull(savedCourse.getId());
        assertEquals(course.getName(), savedCourse.getName());
        assertEquals(course.getCredits(), savedCourse.getCredits());
        assertEquals(course.getCapacity(), savedCourse.getCapacity());
    }

    @Test
    void itShouldSaveMultipleCourses() {
        Course savedCourse1 = courseRepository.save(course);
        Course savedCourse2 = courseRepository.save(course2);

        assertNotNull(savedCourse1.getId());
        assertNotNull(savedCourse2.getId());
        assertEquals(2, courseRepository.findAll().size());
    }

    @Test
    void itShouldFindCourseById() {
        Course savedCourse = courseRepository.save(course);

        Optional<Course> foundCourse = courseRepository.findById(savedCourse.getId());

        assertTrue(foundCourse.isPresent());
        assertEquals(savedCourse.getId(), foundCourse.get().getId());
        assertEquals(savedCourse.getName(), foundCourse.get().getName());
    }

    @Test
    void itShouldFindAllCourses() {
        courseRepository.save(course);
        courseRepository.save(course2);

        List<Course> allCourses = courseRepository.findAll();

        assertEquals(2, allCourses.size());
    }

    @Test
    void itShouldDeleteCourse() {
        Course savedCourse = courseRepository.save(course);
        Long courseId = savedCourse.getId();

        courseRepository.delete(savedCourse);

        assertFalse(courseRepository.existsById(courseId));
    }

    @Test
    void itShouldDeleteCourseById() {
        Course savedCourse = courseRepository.save(course);
        Long courseId = savedCourse.getId();

        courseRepository.deleteById(courseId);

        assertFalse(courseRepository.existsById(courseId));
    }

    @Test
    void itShouldFindByDepartment() {
        courseRepository.save(course);
        courseRepository.save(course2);

        // Create a course in different department
        Course course3 = new Course();
        course3.setName("Networking");
        course3.setDepartment(department2);
        course3.setCredits(3);
        course3.setCapacity(20);
        course3.setTeacher(teacher);
        courseRepository.save(course3);

        List<Course> coursesInDepartment = courseRepository.findByDepartment(department);

        assertEquals(2, coursesInDepartment.size());
        assertTrue(coursesInDepartment.stream().allMatch(c -> c.getDepartment().getId().equals(department.getId())));
    }

    @Test
    void itShouldFindByDepartmentReturnsEmptyListWhenNoCourses() {
        List<Course> coursesInDepartment = courseRepository.findByDepartment(department2);

        assertNotNull(coursesInDepartment);
        assertTrue(coursesInDepartment.isEmpty());
    }

    @Test
    void itShouldFindByTeacherUserName() {
        courseRepository.save(course);
        courseRepository.save(course2);

        List<Course> coursesByTeacher1 = courseRepository.findByTeacherUserName("teacher1");

        assertEquals(1, coursesByTeacher1.size());
        assertEquals("Data Structures", coursesByTeacher1.get(0).getName());
    }

    @Test
    void itShouldFindByTeacherUserNameReturnsEmptyListWhenNoMatch() {
        courseRepository.save(course);

        List<Course> courses = courseRepository.findByTeacherUserName("nonexistent");

        assertNotNull(courses);
        assertTrue(courses.isEmpty());
    }

    @Test
    void itShouldFindMultipleCoursesByTeacherUserName() {
        // Assign both courses to the same teacher
        course2.setTeacher(teacher);
        courseRepository.save(course);
        courseRepository.save(course2);

        List<Course> coursesByTeacher = courseRepository.findByTeacherUserName("teacher1");

        assertEquals(2, coursesByTeacher.size());
    }

    @Test
    void itShouldUpdateCourse() {
        Course savedCourse = courseRepository.save(course);

        savedCourse.setName("Updated Course Name");
        savedCourse.setCredits(5);
        Course updatedCourse = courseRepository.save(savedCourse);

        assertEquals("Updated Course Name", updatedCourse.getName());
        assertEquals(5, updatedCourse.getCredits());
        assertEquals(savedCourse.getId(), updatedCourse.getId());
    }

    @Test
    void itShouldCheckIfCourseExistsById() {
        Course savedCourse = courseRepository.save(course);

        assertTrue(courseRepository.existsById(savedCourse.getId()));
        assertFalse(courseRepository.existsById(999L));
    }

    @Test
    void itShouldCountCourses() {
        assertEquals(0, courseRepository.count());

        courseRepository.save(course);
        assertEquals(1, courseRepository.count());

        courseRepository.save(course2);
        assertEquals(2, courseRepository.count());
    }

    @Test
    void itShouldDeleteAllCourses() {
        courseRepository.save(course);
        courseRepository.save(course2);
        assertEquals(2, courseRepository.count());

        courseRepository.deleteAll();

        assertEquals(0, courseRepository.count());
    }

    @Test
    void itShouldFindStudentsByCourseId() {
        // Save the course first
        Course savedCourse = courseRepository.save(course);

        // Create and save students
        Student student1 = new Student();
        student1.setUserName("student1");
        student1.setEmail("student1@gmail.com");
        student1.setPassword("password");
        student1.setActive(true);
        student1.setGpa(BigDecimal.valueOf(3.5));
        student1.setEnrollmentYear(2024);
        student1.setTotalCredits(30);
        student1 = studentRepository.save(student1);

        Student student2 = new Student();
        student2.setUserName("student2");
        student2.setEmail("student2@gmail.com");
        student2.setPassword("password");
        student2.setActive(true);
        student2.setGpa(BigDecimal.valueOf(3.8));
        student2.setEnrollmentYear(2024);
        student2.setTotalCredits(25);
        student2 = studentRepository.save(student2);

        // Enroll students in the course
        EnrolledCourse enrollment1 = new EnrolledCourse();
        enrollment1.setStudent(student1);
        enrollment1.setCourse(savedCourse);
        enrollment1.setEnrollmentDate(java.time.LocalDateTime.now());
        enrolledCourseRepository.save(enrollment1);

        EnrolledCourse enrollment2 = new EnrolledCourse();
        enrollment2.setStudent(student2);
        enrollment2.setCourse(savedCourse);
        enrollment2.setEnrollmentDate(java.time.LocalDateTime.now());
        enrolledCourseRepository.save(enrollment2);

        // Find students by course ID
        List<Student> students = courseRepository.findStudentsByCourseId(savedCourse.getId());

        assertEquals(2, students.size());
    }

    @Test
    void itShouldFindStudentsByCourseIdReturnsEmptyListWhenNoEnrollments() {
        Course savedCourse = courseRepository.save(course);

        List<Student> students = courseRepository.findStudentsByCourseId(savedCourse.getId());

        assertNotNull(students);
        assertTrue(students.isEmpty());
    }

    @Test
    void itShouldFindStudentsByCourseIdOnlyReturnsStudentsEnrolledInThatCourse() {
        // Save two courses
        Course savedCourse1 = courseRepository.save(course);
        Course savedCourse2 = courseRepository.save(course2);

        // Create and save students
        Student student1 = new Student();
        student1.setUserName("student1");
        student1.setEmail("student1@gmail.com");
        student1.setPassword("password");
        student1.setActive(true);
        student1.setGpa(BigDecimal.valueOf(3.5));
        student1.setEnrollmentYear(2024);
        student1.setTotalCredits(30);
        student1 = studentRepository.save(student1);

        Student student2 = new Student();
        student2.setUserName("student2");
        student2.setEmail("student2@gmail.com");
        student2.setPassword("password");
        student2.setActive(true);
        student2.setGpa(BigDecimal.valueOf(3.8));
        student2.setEnrollmentYear(2024);
        student2.setTotalCredits(25);
        student2 = studentRepository.save(student2);

        // Enroll student1 in course1
        EnrolledCourse enrollment1 = new EnrolledCourse();
        enrollment1.setStudent(student1);
        enrollment1.setCourse(savedCourse1);
        enrollment1.setEnrollmentDate(java.time.LocalDateTime.now());
        enrolledCourseRepository.save(enrollment1);

        // Enroll student2 in course2
        EnrolledCourse enrollment2 = new EnrolledCourse();
        enrollment2.setStudent(student2);
        enrollment2.setCourse(savedCourse2);
        enrollment2.setEnrollmentDate(java.time.LocalDateTime.now());
        enrolledCourseRepository.save(enrollment2);

        // Find students for course1 - should only return student1
        List<Student> studentsInCourse1 = courseRepository.findStudentsByCourseId(savedCourse1.getId());

        assertEquals(1, studentsInCourse1.size());
        assertEquals("student1", studentsInCourse1.get(0).getUserName());

        // Find students for course2 - should only return student2
        List<Student> studentsInCourse2 = courseRepository.findStudentsByCourseId(savedCourse2.getId());

        assertEquals(1, studentsInCourse2.size());
        assertEquals("student2", studentsInCourse2.get(0).getUserName());
    }
}