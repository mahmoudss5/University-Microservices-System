package com.uni.iam.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uni.iam.dto.request.SecurityAuditEventDto;
import com.uni.iam.repository.SecurityAuditLogRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class SecurityAuditServiceImplTest {
    private final SecurityAuditLogRepository repository = mock(SecurityAuditLogRepository.class);
    private final SecurityAuditServiceImpl service = new SecurityAuditServiceImpl(repository, new ObjectMapper());

    @Test
    void storesAuditEvent() {
        var event = new SecurityAuditEventDto("b95e8c88-3e65-4d06-90f1-481be46c2cf6",
                "RATE_LIMIT_EXCEEDED", 1, LocalDateTime.now(), "api-gateway", null,
                "127.0.0.1", "POST", "/api/auth/login", "request-1", "127.0.0.1", null);

        service.record(event);

        verify(repository).save(argThat(log -> log.getEventId().equals(event.eventId())
                && log.getEventType().equals("RATE_LIMIT_EXCEEDED")
                && log.getClientIp().equals("127.0.0.1")));
    }

    @Test
    void ignoresAlreadyStoredEvent() {
        when(repository.existsByEventId("existing-event")).thenReturn(true);
        var event = new SecurityAuditEventDto("existing-event", "COURSE_CREATED", 1,
                LocalDateTime.now(), "academic-core", null, null, null, null, null, "10", null);

        service.record(event);

        verify(repository, never()).save(any());
    }
}
