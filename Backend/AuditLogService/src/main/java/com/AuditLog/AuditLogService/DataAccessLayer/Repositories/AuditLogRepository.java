package com.AuditLog.AuditLogService.DataAccessLayer.Repositories;

import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditEventType;
import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditLog;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    boolean existsByEventId(String eventId);

    Optional<AuditLog> findByEventId(String eventId);

    Page<AuditLog> findByEventType(AuditEventType eventType, Pageable pageable);

    Page<AuditLog> findBySource(String source, Pageable pageable);

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);
}
