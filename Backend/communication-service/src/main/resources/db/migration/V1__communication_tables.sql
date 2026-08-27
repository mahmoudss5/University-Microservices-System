CREATE TABLE user_snapshots (
    user_id    BIGINT       NOT NULL PRIMARY KEY,
    user_name  VARCHAR(255) NOT NULL,
    user_role  VARCHAR(20)  NOT NULL,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE course_snapshots (
    course_id   BIGINT       NOT NULL PRIMARY KEY,
    course_name VARCHAR(255) NOT NULL,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE enrollment_snapshots (
    id          BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    student_id  BIGINT   NOT NULL,
    course_id   BIGINT   NOT NULL,
    enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_enrollment_snapshots_student_course UNIQUE (student_id, course_id),
    CONSTRAINT fk_enrollment_snapshots_student
        FOREIGN KEY (student_id) REFERENCES user_snapshots(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_snapshots_course
        FOREIGN KEY (course_id) REFERENCES course_snapshots(course_id) ON DELETE CASCADE,
    INDEX idx_enrollment_snapshots_course_id (course_id)
);

CREATE TABLE messages (
    id         BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    course_id  BIGINT   NOT NULL,
    sender_id  BIGINT   NOT NULL,
    content    TEXT     NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_course_snapshot
        FOREIGN KEY (course_id) REFERENCES course_snapshots(course_id),
    CONSTRAINT fk_messages_sender_snapshot
        FOREIGN KEY (sender_id) REFERENCES user_snapshots(user_id),
    INDEX idx_messages_course_created_at (course_id, created_at),
    INDEX idx_messages_sender_created_at (sender_id, created_at)
);

CREATE TABLE notifications (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(255) NOT NULL,
    message    TEXT         NOT NULL,
    type       VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM',
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user_snapshot
        FOREIGN KEY (user_id) REFERENCES user_snapshots(user_id),
    INDEX idx_notifications_user_created_at (user_id, created_at),
    INDEX idx_notifications_user_read_created_at (user_id, is_read, created_at),
    INDEX idx_notifications_user_type_created_at (user_id, type, created_at)
);
