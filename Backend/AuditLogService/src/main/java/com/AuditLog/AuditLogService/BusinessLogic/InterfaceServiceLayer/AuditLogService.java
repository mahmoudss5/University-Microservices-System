package com.AuditLog.AuditLogService.BusinessLogic.InterfaceServiceLayer;

import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditEventType;
import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {


    AuditLog record(AuditLog auditLog);

    AuditLog getById(Long id);

    AuditLog getByEventId(String eventId);

    Page<AuditLog> findAll(
            AuditEventType eventType,
            String source,
            Long userId,
            Pageable pageable);

    void deleteById(Long id);
}
