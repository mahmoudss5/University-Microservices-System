package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Entities.EnrolledCourse;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrolledCourseRepository extends JpaRepository<EnrolledCourse, Long> {
    List<EnrolledCourse> findByStudent(User student);

    List<EnrolledCourse> findByCourse(Course course);

    boolean existsByStudentAndCourse(User student, Course course);
    Optional<EnrolledCourse> findById(Long id);
}
