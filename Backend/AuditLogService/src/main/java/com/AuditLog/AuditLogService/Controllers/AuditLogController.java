package com.AuditLog.AuditLogService.Controllers;

import com.AuditLog.AuditLogService.BusinessLogic.InterfaceServiceLayer.AuditLogService;
import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditEventType;
import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditLog;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<Page<AuditLog>> findAll(
            @RequestParam(required = false) AuditEventType eventType,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Long userId,
            @PageableDefault(
                    size = 20,
                    sort = "occurredAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.findAll(eventType, source, userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(auditLogService.getById(id));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<AuditLog> findByEventId(@PathVariable String eventId) {
        try {
            return ResponseEntity.ok(auditLogService.getByEventId(eventId));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
