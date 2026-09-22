CREATE TABLE audit_logs (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id        VARCHAR(36)  NOT NULL,
    event_type      VARCHAR(80)  NOT NULL,
    event_version   INT          NOT NULL,
    occurred_at     DATETIME(6)  NOT NULL,
    source          VARCHAR(80)  NOT NULL,
    user_id         BIGINT       NULL,
    client_ip       VARCHAR(45)  NULL,
    http_method     VARCHAR(10)  NULL,
    request_path    VARCHAR(512) NULL,
    correlation_id  VARCHAR(100) NULL,
    aggregate_id    VARCHAR(100) NULL,
    details         JSON         NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT uk_audit_logs_event_id UNIQUE (event_id),
    INDEX idx_audit_logs_event_type_time (event_type, occurred_at),
    INDEX idx_audit_logs_user_time (user_id, occurred_at),
    INDEX idx_audit_logs_source_time (source, occurred_at),
    INDEX idx_audit_logs_correlation_id (correlation_id)
);
