package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.Notification;
import UnitSystem.demo.DataAccessLayer.Entities.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    List<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type);

    long countByRecipientIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :userId AND n.isRead = false")
    int markAllAsReadForUser(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient.id = :userId")
    void deleteAllByRecipientId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = false")
    int countHowManyNotificationsAreUnreadForUser(Long userId);
}
