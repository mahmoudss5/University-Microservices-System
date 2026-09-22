package com.AuditLog.AuditLogService.DataAccessLayer.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 80)
    private AuditEventType eventType;

    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false, length = 80)
    private String source;

    /** ID from IAM or Academic Core; intentionally has no cross-service FK. */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "request_path", length = 512)
    private String requestPath;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "aggregate_id", length = 100)
    private String aggregateId;

    /** Serialized event-specific payload stored in the MySQL JSON column. */
    @Column(columnDefinition = "json")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void setCreatedAtIfMissing() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public AuditLog() {
    }

    public AuditLog(Long id, String eventId, AuditEventType eventType, Integer eventVersion,
                    LocalDateTime occurredAt, String source, Long userId, String clientIp,
                    String httpMethod, String requestPath, String correlationId,
                    String aggregateId, String details, LocalDateTime createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.occurredAt = occurredAt;
        this.source = source;
        this.userId = userId;
        this.clientIp = clientIp;
        this.httpMethod = httpMethod;
        this.requestPath = requestPath;
        this.correlationId = correlationId;
        this.aggregateId = aggregateId;
        this.details = details;
        this.createdAt = createdAt;
    }

}
