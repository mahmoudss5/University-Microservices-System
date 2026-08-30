package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository;

import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.OutboxEventEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {
    Optional<OutboxEventEntity> findByEventId(UUID eventId);

    @Query(value = """
            SELECT * FROM outbox_events
            WHERE ((event_status = 'PENDING' AND next_attempt_at <= :now)
                OR (event_status = 'PROCESSING' AND claimed_at < :staleBefore))
            ORDER BY id
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEventEntity> lockPublishableBatch(@Param("batchSize") int batchSize,
                                                  @Param("now") LocalDateTime now,
                                                  @Param("staleBefore") LocalDateTime staleBefore);
}
