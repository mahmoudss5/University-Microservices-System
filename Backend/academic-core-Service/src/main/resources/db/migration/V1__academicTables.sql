
CREATE TABLE IF NOT EXISTS departments (
id   BIGINT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(255) NOT NULL UNIQUE
);

create table if not exists courses(
     id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    course_code    VARCHAR(255) NOT NULL UNIQUE,
    description    TEXT,
    start_date     DATE,
    end_date       DATE,
    credits        INT NOT NULL,
    max_students   INT NOT NULL,
    enrolled_count INT NOT NULL DEFAULT 0,
    department_id  BIGINT NOT NULL,
    teacher_id     BIGINT NOT NULL,
    CONSTRAINT fk_courses_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    INDEX idx_courses_teacher_id (teacher_id),
    INDEX idx_courses_department_id (department_id)

);

CREATE TABLE IF NOT EXISTS course_prerequisites (
    course_id           BIGINT NOT NULL,
    course_prerequisite BIGINT NOT NULL,
    PRIMARY KEY (course_id, course_prerequisite),
    CONSTRAINT fk_prereq_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_prereq_prerequisite FOREIGN KEY (course_prerequisite) REFERENCES courses(id) ON DELETE RESTRICT,
    INDEX idx_prerequisite_course (course_prerequisite)
    );

CREATE TABLE IF NOT EXISTS enrollments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id  BIGINT NOT NULL,
    enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',
    grade DECIMAL(5,2),
    passed BOOLEAN,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT unique_student_course UNIQUE (student_id, course_id),
    INDEX idx_enrollments_student_id (student_id),
    INDEX idx_enrollments_completion (student_id, course_id, status, passed)
    );

CREATE TABLE IF NOT EXISTS announcements (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    course_id   BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcements_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_announcements_course_id (course_id)
    );

CREATE TABLE IF NOT EXISTS feedback (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    course_id  BIGINT NOT NULL,
    comment    TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_feedback_user_id (user_id)
    );

CREATE TABLE IF NOT EXISTS users_snapshot(
    user_id BIGINT NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    user_role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    INDEX idx_users_snapshot_role_active (user_role, active)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT,
    user_name  VARCHAR(255),
    user_role  VARCHAR(20),
    action     VARCHAR(255) NOT NULL,
    details    TEXT,
    ip_address VARCHAR(50),
    created_at DATETIME NOT NULL,
    INDEX idx_audit_logs_user_id (user_id),
    INDEX idx_audit_logs_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS outbox_events(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    event_data JSON NOT NULL,
    event_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_error TEXT,
    claimed_at DATETIME,
    claimed_by VARCHAR(100),
    processed_at DATETIME,
    INDEX idx_outbox_pending (event_status, next_attempt_at, id),
    INDEX idx_outbox_stale_claims (event_status, claimed_at)
);
