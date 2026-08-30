package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.EnrollmentSnapshot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentSnapshotRepository
        extends JpaRepository<EnrollmentSnapshot, Long> {

    List<EnrollmentSnapshot> findByCourseId(Long courseId);

    Optional<EnrollmentSnapshot> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );

    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
}
