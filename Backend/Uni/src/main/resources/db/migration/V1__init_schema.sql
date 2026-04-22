-- 1. Tables Creation
CREATE TABLE `users` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT,
                          `email` VARCHAR(255) UNIQUE NOT NULL,
                         `user_name` VARCHAR(255) UNIQUE NOT NULL,
                         `password_hash` VARCHAR(255) NOT NULL,
                         `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `active` BOOLEAN NOT NULL DEFAULT true,
                         PRIMARY KEY (`id`)
);

-- Child table for Students
CREATE TABLE `students` (
                            `user_id` BIGINT NOT NULL,
                            `gpa` DECIMAL(3,2),
                            `enrollment_year` INT,
                            `total_credits` INT,
                            PRIMARY KEY (`user_id`)
);

-- Child table for Teachers
CREATE TABLE `teachers` (
                            `user_id` BIGINT NOT NULL,
                            `office_location` VARCHAR(255),
                            `salary` DECIMAL(10,2),
                            PRIMARY KEY (`user_id`)
);

CREATE TABLE `roles` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT,
                         `name` VARCHAR(256) UNIQUE NOT NULL,
                         PRIMARY KEY (`id`)
);

CREATE TABLE `user_roles` (
                              `user_id` BIGINT NOT NULL,
                              `role_id` BIGINT NOT NULL,
                              PRIMARY KEY (`user_id`, `role_id`)
);

CREATE TABLE `department` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT,
                              `dep_name` VARCHAR(255) UNIQUE NOT NULL,
                              PRIMARY KEY (`id`)
);

CREATE TABLE `teacher_department` (
                                      `teacher_id` BIGINT NOT NULL,
                                      `dep_id` BIGINT NOT NULL,
                                      PRIMARY KEY (`teacher_id`, `dep_id`)
);

CREATE TABLE `courses` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT,
                           `course_name` VARCHAR(255),
                           `course_dep` BIGINT NOT NULL,
                           `teacher_id` BIGINT NOT NULL,
                           `credits` INT NOT NULL,
                           `capacity` INT NOT NULL,
                           PRIMARY KEY (`id`)
);

CREATE TABLE `course_prerequisites` (
                                        `course_id` BIGINT NOT NULL,
                                        `course_prerequisite` BIGINT NOT NULL,
                                        PRIMARY KEY (`course_id`, `course_prerequisite`)
);

CREATE TABLE `enrolled_courses` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                                    `student_id` BIGINT NOT NULL,
                                    `course_id` BIGINT NOT NULL,
                                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`)
);

CREATE TABLE `audit_logs` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT,
                              `user_id` BIGINT,
                              `action` VARCHAR(255) NOT NULL,
                              `details` TEXT,
                              `ip_address` VARCHAR(50),
                              `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`)
);

-- 2. Foreign Key Constraints

-- Inheritance Links
ALTER TABLE `students` ADD CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
ALTER TABLE `teachers` ADD CONSTRAINT `fk_teacher_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

-- Roles
ALTER TABLE `user_roles` ADD CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
ALTER TABLE `user_roles` ADD CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE;

-- Departments (Notice: Linked to teachers table, not users)
ALTER TABLE `teacher_department` ADD CONSTRAINT `fk_teacher_dep_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teachers` (`user_id`) ON DELETE CASCADE;
ALTER TABLE `teacher_department` ADD CONSTRAINT `fk_teacher_dep_dep` FOREIGN KEY (`dep_id`) REFERENCES `department` (`id`) ON DELETE CASCADE;

-- Courses
ALTER TABLE `courses` ADD CONSTRAINT `fk_courses_dep` FOREIGN KEY (`course_dep`) REFERENCES `department` (`id`) ON DELETE CASCADE;
ALTER TABLE `courses` ADD CONSTRAINT `fk_courses_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teachers` (`user_id`) ON DELETE CASCADE;

-- Prerequisites
ALTER TABLE `course_prerequisites` ADD CONSTRAINT `fk_prereq_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE;
ALTER TABLE `course_prerequisites` ADD CONSTRAINT `fk_prereq_prereq` FOREIGN KEY (`course_prerequisite`) REFERENCES `courses` (`id`) ON DELETE CASCADE;

-- Enrollments (Notice: Linked to students table, not users)
ALTER TABLE `enrolled_courses` ADD CONSTRAINT `fk_enrolled_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`user_id`) ON DELETE CASCADE;
ALTER TABLE `enrolled_courses` ADD CONSTRAINT `fk_enrolled_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE;

-- Audit Logs
ALTER TABLE `audit_logs` ADD CONSTRAINT `fk_audit_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

