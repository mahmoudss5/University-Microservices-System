package com.AuditLog.AuditLogService.BusinessLogic.ImpServiceLayer;

import com.AuditLog.AuditLogService.BusinessLogic.InterfaceServiceLayer.AuditLogService;
import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditEventType;
import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditLog;
import com.AuditLog.AuditLogService.DataAccessLayer.Repositories.AuditLogRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public AuditLog record(AuditLog auditLog) {
        validate(auditLog);

        return auditLogRepository.findByEventId(auditLog.getEventId())
                .orElseGet(() -> {
                    AuditLog saved = auditLogRepository.save(auditLog);
                    log.debug("Stored audit event {} of type {}", saved.getEventId(), saved.getEventType());
                    return saved;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLog getById(Long id) {
        Objects.requireNonNull(id, "Audit log id is required");
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Audit log not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLog getByEventId(String eventId) {
        requireText(eventId, "eventId");
        return auditLogRepository.findByEventId(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Audit event not found: " + eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findAll(
            AuditEventType eventType,
            String source,
            Long userId,
            Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable is required");

        Specification<AuditLog> specification = (root, query, criteriaBuilder) ->
                criteriaBuilder.conjunction();

        if (eventType != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                            root.get("eventType"), eventType));
        }
        if (source != null && !source.isBlank()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                            root.get("source"), source.trim()));
        }
        if (userId != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                            root.get("userId"), userId));
        }

        return auditLogRepository.findAll(specification, pageable);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        getById(id);
        auditLogRepository.deleteById(id);
    }

    private void validate(AuditLog auditLog) {
        Objects.requireNonNull(auditLog, "Audit log is required");
        requireText(auditLog.getEventId(), "eventId");
        if (auditLog.getEventType() == null) {
            throw new IllegalArgumentException("eventType is required");
        }
        if (auditLog.getEventVersion() == null || auditLog.getEventVersion() < 1) {
            throw new IllegalArgumentException("eventVersion must be positive");
        }
        if (auditLog.getOccurredAt() == null) {
            throw new IllegalArgumentException("occurredAt is required");
        }
        requireText(auditLog.getSource(), "source");
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
