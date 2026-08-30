package com.uni.iam.repository;

import com.uni.iam.entity.SecurityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {
    boolean existsByEventId(String eventId);
    Page<SecurityAuditLog> findByEventType(String eventType, Pageable pageable);
}
