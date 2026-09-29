CREATE TABLE IF NOT EXISTS course_materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    s3_key VARCHAR(512) NOT NULL UNIQUE,
    material_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    content_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    uploaded_at DATETIME NULL,
    CONSTRAINT fk_course_materials_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_course_materials_course_id (course_id),
    INDEX idx_course_materials_status (content_status)
);
