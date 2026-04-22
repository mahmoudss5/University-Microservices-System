package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.EventType;
import UnitSystem.demo.DataAccessLayer.Entities.UpcomingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UpcomingEventRepository extends JpaRepository<UpcomingEvent, Long> {

    List<UpcomingEvent> findByType(EventType type);

    List<UpcomingEvent> findByUserId(Long userId);

    @Query("SELECT e FROM UpcomingEvent e WHERE e.eventDate >= :from ORDER BY e.eventDate ASC")
    List<UpcomingEvent> findUpcomingFromDate(@Param("from") LocalDateTime from);

    @Query("SELECT e FROM UpcomingEvent e WHERE e.eventDate BETWEEN :from AND :to ORDER BY e.eventDate ASC")
    List<UpcomingEvent> findByEventDateBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);


}
