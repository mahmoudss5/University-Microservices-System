ALTER TABLE courses
    ADD COLUMN course_description VARCHAR(500),
    ADD COLUMN course_code VARCHAR(20),
    ADD COLUMN start_date DATE DEFAULT (CURRENT_DATE),
    ADD COLUMN end_date DATE DEFAULT (CURRENT_DATE),
    ADD CONSTRAINT unique_course_code UNIQUE (course_code),
    ADD CONSTRAINT check_dates CHECK (end_date >= start_date);