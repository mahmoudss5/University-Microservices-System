package com.uni.iam.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uni.iam.service.interfaces.SecurityAuditService;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SecurityAuditEventConsumerTest {
    private final SecurityAuditService service = mock(SecurityAuditService.class);
    private final SecurityAuditEventConsumer consumer = new SecurityAuditEventConsumer(service);
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void consumesAcademicEnvelope() throws Exception {
        consumer.consumeAcademicAudit(mapper.readTree("""
                {"eventId":"885c98da-a7df-4a02-945d-c86bb47bbdf5","eventType":"STUDENT_ENROLLED",
                 "eventVersion":1,"occurredAt":"2026-08-30T15:00:00","aggregateId":"22",
                 "event":{"studentId":7,"enrolledCourseId":22,"courseName":"Databases"}}
                """));

        verify(service).record(argThat(event -> event.source().equals("academic-core")
                && event.userId().equals(7L) && event.aggregateId().equals("22")));
    }

    @Test
    void consumesGatewayEnvelope() throws Exception {
        consumer.consumeGatewayAudit(mapper.readTree("""
                {"eventId":"885c98da-a7df-4a02-945d-c86bb47bbdf5","eventType":"RATE_LIMIT_EXCEEDED",
                 "eventVersion":1,"occurredAt":"2026-08-30T15:00:00","source":"api-gateway",
                 "clientIp":"127.0.0.1","httpMethod":"POST","requestPath":"/api/auth/login",
                 "event":{"policy":"route:iam-service-auth","limit":20,"windowSeconds":60}}
                """));

        verify(service).record(argThat(event -> event.source().equals("api-gateway")
                && event.clientIp().equals("127.0.0.1")));
    }
}
