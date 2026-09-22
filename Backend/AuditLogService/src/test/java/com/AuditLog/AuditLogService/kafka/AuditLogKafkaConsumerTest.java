package com.AuditLog.AuditLogService.kafka;

import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditEventType;
import com.AuditLog.AuditLogService.DataAccessLayer.Entities.AuditLog;
import com.AuditLog.AuditLogService.BusinessLogic.InterfaceServiceLayer.AuditLogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuditLogKafkaConsumerTest {

    private final RecordingAuditLogService auditLogService = new RecordingAuditLogService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuditLogKafkaConsumer consumer = new AuditLogKafkaConsumer(auditLogService, objectMapper);

    @Test
    void consumesAcademicEnvelope() throws Exception {
        consumer.consume(record("course-created", "42", """
                {"eventId":"event-1","eventType":"COURSE_CREATED","eventVersion":1,
                 "occurredAt":"2026-09-21T18:00:00","aggregateId":"42",
                 "event":{"courseId":"42","courseName":"Databases"}}
                """));

        assertEquals("event-1", auditLogService.last.getEventId());
        assertEquals(AuditEventType.COURSE_CREATED, auditLogService.last.getEventType());
        assertEquals("42", auditLogService.last.getAggregateId());
        assertEquals("academic-core", auditLogService.last.getSource());
    }

    @Test
    void consumesFlatIamEvent() throws Exception {
        consumer.consume(record("user-updated-v1", "7", """
                {"userId":7,"username":"student","email":"student@example.com",
                 "role":"STUDENT","active":true}
                """));

        assertNotNull(auditLogService.last.getEventId());
        assertEquals(AuditEventType.USER_UPDATED, auditLogService.last.getEventType());
        assertEquals(7L, auditLogService.last.getUserId());
        assertEquals("7", auditLogService.last.getAggregateId());
        assertEquals("iam-service", auditLogService.last.getSource());
    }

    private ConsumerRecord<String, JsonNode> record(String topic, String key, String json) throws Exception {
        return new ConsumerRecord<>(topic, 0, 12L, key, objectMapper.readTree(json));
    }

    private static final class RecordingAuditLogService implements AuditLogService {
        private AuditLog last;

        @Override
        public AuditLog record(AuditLog auditLog) {
            last = auditLog;
            return auditLog;
        }

        @Override
        public AuditLog getById(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AuditLog getByEventId(String eventId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Page<AuditLog> findAll(AuditEventType eventType, String source, Long userId, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(Long id) {
            throw new UnsupportedOperationException();
        }
    }
}
