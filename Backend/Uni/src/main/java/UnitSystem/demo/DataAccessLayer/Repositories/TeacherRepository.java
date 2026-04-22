package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
        Teacher findByUserName(String userName);
}
