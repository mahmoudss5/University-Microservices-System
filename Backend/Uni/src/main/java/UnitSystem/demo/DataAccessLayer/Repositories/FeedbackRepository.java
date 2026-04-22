package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.Feedback;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByUser(User user);

    List<Feedback> findByUserId(Long userId);

    List<Feedback> findByRole(String role);

    @Query("SELECT f FROM Feedback f ORDER BY f.createdAt DESC")
    List<Feedback> findAllOrderByCreatedAtDesc();
}
