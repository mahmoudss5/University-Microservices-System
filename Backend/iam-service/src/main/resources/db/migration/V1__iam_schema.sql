CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_users_role (role)
);

CREATE TABLE students (
    user_id BIGINT PRIMARY KEY,
    gpa DECIMAL(3,2),
    enrollment_date DATE,
    total_credits INT,
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE teachers (
    user_id BIGINT PRIMARY KEY,
    office_location VARCHAR(50),
    salary DECIMAL(10,2),
    CONSTRAINT fk_teachers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE admins (
    user_id BIGINT PRIMARY KEY,
    CONSTRAINT fk_admins_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE security_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    event_version INT NOT NULL,
    occurred_at DATETIME(6) NOT NULL,
    source VARCHAR(80) NOT NULL,
    user_id BIGINT NULL,
    client_ip VARCHAR(45) NULL,
    http_method VARCHAR(10) NULL,
    request_path VARCHAR(512) NULL,
    correlation_id VARCHAR(100) NULL,
    aggregate_id VARCHAR(100) NULL,
    details JSON NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_security_audit_event_id UNIQUE (event_id),
    INDEX idx_security_audit_type_time (event_type, occurred_at),
    INDEX idx_security_audit_user_time (user_id, occurred_at),
    INDEX idx_security_audit_ip_time (client_ip, occurred_at),
    INDEX idx_security_audit_source_time (source, occurred_at)
);
