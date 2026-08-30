-- Development-only sample data. This directory is intentionally outside Flyway's
-- production location (classpath:db/migration). Apply explicitly in local environments.
INSERT INTO users_snapshot (user_id, user_name, user_role, active) VALUES
    (1, 'admin_super', 'ADMIN', TRUE),
    (2, 'dr_ahmed', 'TEACHER', TRUE),
    (3, 'dr_sara', 'TEACHER', TRUE),
    (4, 'student_ali', 'STUDENT', TRUE),
    (5, 'student_mona', 'STUDENT', TRUE)
ON DUPLICATE KEY UPDATE
    user_name = VALUES(user_name), user_role = VALUES(user_role), active = VALUES(active);

INSERT IGNORE INTO departments (name) VALUES
    ('Information_Systems'), ('Information_Technology'), ('Computer_Science'),
    ('Software_Engineering'), ('Data_Science'), ('Artificial_Intelligence'), ('Cybersecurity');
