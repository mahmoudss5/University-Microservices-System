package com.uni.iam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;
    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;
    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
    @Column(nullable = false, length = 80)
    private String source;
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
    @Column(columnDefinition = "json")
    private String details;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
