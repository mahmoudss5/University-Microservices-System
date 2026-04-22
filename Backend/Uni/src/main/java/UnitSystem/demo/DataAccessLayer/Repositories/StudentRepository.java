package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Entities.Student;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

  @Query("SELECT e.course FROM EnrolledCourse e WHERE e.student.id = :studentId")
  List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);

  Student findByUserName(String userName);
}
