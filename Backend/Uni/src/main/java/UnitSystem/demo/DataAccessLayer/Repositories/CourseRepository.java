package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Entities.Department;
import UnitSystem.demo.DataAccessLayer.Entities.Student;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByDepartment(Department department);

    List<Course> findByDepartmentName(String departmentName);

    List<Course> findByTeacherUserName(String userName);

    List<Course> findByTeacherId(Long teacherId);

    @Query("SELECT e.student FROM EnrolledCourse e WHERE e.course.id = :courseId")
    List<Student> findStudentsByCourseId(@Param("courseId") Long courseId);

    Optional<Course> findByName(String name);

    @Query("SELECT c FROM Course c LEFT JOIN c.courseEnrollments e GROUP BY c ORDER BY COUNT(e) DESC")
    List<Course> findTopPopularCourses();

    @Query("SELECT s.email FROM Student s JOIN EnrolledCourse ec ON s.id = ec.student.id WHERE ec.course.id = :courseId")
    List<String> findStudentEmailsByCourseId(@Param("courseId") Long courseId);


}
