package com.uni.iam.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uni.iam.dto.request.SecurityAuditEventDto;
import com.uni.iam.entity.SecurityAuditLog;
import com.uni.iam.repository.SecurityAuditLogRepository;
import com.uni.iam.service.interfaces.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SecurityAuditServiceImpl implements SecurityAuditService {
    private final SecurityAuditLogRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void record(SecurityAuditEventDto event) {
        validate(event);
        if (repository.existsByEventId(event.eventId())) return;
        repository.save(SecurityAuditLog.builder()
                .eventId(event.eventId()).eventType(event.eventType()).eventVersion(event.eventVersion())
                .occurredAt(event.occurredAt()).source(event.source()).userId(event.userId())
                .clientIp(event.clientIp()).httpMethod(event.httpMethod()).requestPath(event.requestPath())
                .correlationId(event.correlationId()).aggregateId(event.aggregateId())
                .details(toJson(event.event())).build());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SecurityAuditLog> findAll(String eventType, Pageable pageable) {
        return eventType == null || eventType.isBlank()
                ? repository.findAll(pageable)
                : repository.findByEventType(eventType, pageable);
    }

    private void validate(SecurityAuditEventDto event) {
        if (event == null || event.eventId() == null || event.eventId().isBlank())
            throw new IllegalArgumentException("eventId is required");
        if (event.eventType() == null || event.eventType().isBlank())
            throw new IllegalArgumentException("eventType is required");
        if (event.eventVersion() != 1) throw new IllegalArgumentException("Unsupported event version");
        if (event.occurredAt() == null) throw new IllegalArgumentException("occurredAt is required");
        if (event.source() == null || event.source().isBlank()) throw new IllegalArgumentException("source is required");
    }

    private String toJson(Object value) {
        try {
            return value == null ? null : objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid audit event payload", exception);
        }
    }
}
