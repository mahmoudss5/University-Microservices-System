-- =============================================================
-- Sample Data for UniSystem
-- Password for all users: "password"  (BCrypt hash below)
-- Roles are seeded automatically by DataSeeder on startup:
--   Student (id auto), Teacher (id auto), Admin (id auto)
-- =============================================================

-- ---------------------------------------------------------------
-- 1. Users  (user_name, email, password_hash, active, created_at)
-- ---------------------------------------------------------------
INSERT INTO users (id, user_name, email, password_hash, active, created_at) VALUES
(1, 'john_doe',      'john.doe@university.edu',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkdaWK.i', true, CURRENT_TIMESTAMP),
(2, 'jane_smith',    'jane.smith@university.edu',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkdaWK.i', true, CURRENT_TIMESTAMP),
(3, 'dr_johnson',    'dr.johnson@university.edu',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkdaWK.i', true, CURRENT_TIMESTAMP),
(4, 'prof_williams', 'prof.williams@university.edu',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkdaWK.i', true, CURRENT_TIMESTAMP),
(5, 'admin_user',    'admin@university.edu',          '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkdaWK.i', true, CURRENT_TIMESTAMP),
(6, 'mike_brown',    'mike.brown@university.edu',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkdaWK.i', true, CURRENT_TIMESTAMP),
(7, 'sarah_davis',   'sarah.davis@university.edu',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkdaWK.i', true, CURRENT_TIMESTAMP),
(8, 'dr_lee',        'dr.lee@university.edu',         '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkdaWK.i', true, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 2. Students  (user_id FK → users, gpa, enrollment_year, total_credits)
-- ---------------------------------------------------------------
INSERT INTO students (user_id, gpa, enrollment_year, total_credits) VALUES
(1, 3.80, 2023, 90),
(2, 3.50, 2023, 85),
(6, 3.90, 2024, 60),
(7, 3.65, 2024, 65);

-- ---------------------------------------------------------------
-- 3. Teachers  (user_id FK → users, office_location, salary)
-- ---------------------------------------------------------------
INSERT INTO teachers (user_id, office_location, salary) VALUES
(3, 'Building A, Room 201', 75000.00),
(4, 'Building B, Room 105', 80000.00),
(8, 'Building A, Room 310', 72000.00);

-- ---------------------------------------------------------------
-- 4. User Roles  (role names are seeded by DataSeeder: Student, Teacher, Admin)
-- ---------------------------------------------------------------
INSERT INTO user_roles (user_id, role_id)
SELECT 1, id FROM roles WHERE name = 'Student';   -- john_doe

INSERT INTO user_roles (user_id, role_id)
SELECT 2, id FROM roles WHERE name = 'Student';   -- jane_smith

INSERT INTO user_roles (user_id, role_id)
SELECT 3, id FROM roles WHERE name = 'Teacher';   -- dr_johnson

INSERT INTO user_roles (user_id, role_id)
SELECT 4, id FROM roles WHERE name = 'Teacher';   -- prof_williams

INSERT INTO user_roles (user_id, role_id)
SELECT 5, id FROM roles WHERE name = 'Admin';     -- admin_user

INSERT INTO user_roles (user_id, role_id)
SELECT 6, id FROM roles WHERE name = 'Student';   -- mike_brown

INSERT INTO user_roles (user_id, role_id)
SELECT 7, id FROM roles WHERE name = 'Student';   -- sarah_davis

INSERT INTO user_roles (user_id, role_id)
SELECT 8, id FROM roles WHERE name = 'Teacher';   -- dr_lee

-- ---------------------------------------------------------------
-- 5. Departments  (dep_name)
-- ---------------------------------------------------------------
INSERT INTO department (id, dep_name) VALUES
(1, 'Computer Science'),
(2, 'Mathematics'),
(3, 'English');

-- ---------------------------------------------------------------
-- 6. Courses  (course_name, course_description, course_dep FK → department,
--              teacher_id FK → teachers, credits, capacity)
--   No course_code column in this schema.
-- ---------------------------------------------------------------
INSERT INTO courses (id, course_name, course_description, course_dep, teacher_id, credits, capacity,course_code,start_date,end_date) VALUES
(1, 'Introduction to Computer Science', 'Fundamental concepts of programming and computer science', 1, 3, 3, 40, 'CS101', '2026-02-01', '2026-06-01'),
(2, 'Data Structures',                  'Advanced data structures and algorithms',                  1, 3, 4, 35 , 'CS201', '2026-02-01', '2026-06-01'),
(3, 'Calculus I',                       'Differential and integral calculus',                       2, 4, 4, 50  , 'MATH101', '2026-02-01', '2026-06-01'),
(4, 'English Composition',              'Academic writing and composition',                         3, 4, 3, 45 , 'ENG101', '2026-02-01', '2026-06-01'),
(5, 'Database Systems',                 'Relational database design and SQL',                       1, 8, 3, 30 , 'CS301', '2026-02-01', '2026-06-01'),
(6, 'Software Engineering',             'Software development methodologies',                       1, 8, 4, 35 , 'CS401', '2026-02-01', '2026-06-01');

-- ---------------------------------------------------------------
-- 7. Enrollments  (student_id FK → students, course_id FK → courses, created_at)
--   No grade column in this schema.
-- ---------------------------------------------------------------
INSERT INTO enrolled_courses (id, student_id, course_id, created_at) VALUES
(1,  1, 1, CURRENT_TIMESTAMP),
(2,  1, 2, CURRENT_TIMESTAMP),
(3,  2, 1, CURRENT_TIMESTAMP),
(4,  2, 3, CURRENT_TIMESTAMP),
(5,  6, 1, CURRENT_TIMESTAMP),
(6,  6, 4, CURRENT_TIMESTAMP),
(7,  7, 2, CURRENT_TIMESTAMP),
(8,  7, 5, CURRENT_TIMESTAMP),
(9,  1, 5, CURRENT_TIMESTAMP),
(10, 2, 6, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 8. Feedbacks  (user_id FK → users, role varchar, comment)
--   No course_id or rating in this schema.
-- ---------------------------------------------------------------
INSERT INTO feedbacks (id, user_id, role, comment, created_at) VALUES
(1, 1, 'Student', 'Excellent experience! The courses are very informative and well-structured.', CURRENT_TIMESTAMP),
(2, 2, 'Student', 'Good overall, but could use more practical examples in some modules.',        CURRENT_TIMESTAMP),
(3, 1, 'Student', 'Challenging curriculum but very rewarding. Learned a great deal!',            CURRENT_TIMESTAMP),
(4, 6, 'Student', 'Professors explain concepts clearly and are always available for help.',       CURRENT_TIMESTAMP),
(5, 7, 'Student', 'Great content and well-organised. Would recommend to other students.',         CURRENT_TIMESTAMP),
(6, 2, 'Student', 'The system is okay but could use better organisation in some areas.',          CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 9. Announcements  (course_id FK → courses, title, description)
--   No author_id or priority in this schema — linked to a course.
-- ---------------------------------------------------------------
INSERT INTO announcements (id, course_id, title, description, created_at) VALUES
(1, 1, 'Welcome to Spring Semester 2026',  'Welcome everyone to CS101! Please review the syllabus and course materials on the portal.', CURRENT_TIMESTAMP),
(2, 1, 'Assignment 1 Due Next Week',       'Reminder: Assignment 1 is due on March 9th. Submit via the student portal by midnight.',     CURRENT_TIMESTAMP),
(3, 2, 'Mid-term Exam Date Confirmed',     'The mid-term exam for Data Structures will be held on March 25th in Lab 101 at 10 AM.',      CURRENT_TIMESTAMP),
(4, 5, 'Lab Session Rescheduled',          'This week''s Database Systems lab is moved to Thursday at 3 PM in Lab 202.',                 CURRENT_TIMESTAMP),
(5, 6, 'Guest Lecture on Agile Methods',   'We will have a guest speaker on Agile development practices on March 18th at 2 PM.',         CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 10. Upcoming Events  (title, subtitle, event_date datetime, type, user_id FK → users)
--    type must be one of: HIGH_PRIORITY | EXAM | EVENT
--    Each event is owned by a specific user.
-- ---------------------------------------------------------------
INSERT INTO upcoming_events (id, title, subtitle, event_date, type, user_id, created_at) VALUES
(1, 'Tech Talk: AI and Machine Learning', 'Guest speaker from the tech industry — Auditorium A',      '2026-03-15 14:00:00', 'EVENT',         1, CURRENT_TIMESTAMP),
(2, 'Student Club Fair',                  'Explore student organisations — Main Campus Plaza',          '2026-03-10 10:00:00', 'EVENT',         2, CURRENT_TIMESTAMP),
(3, 'Coding Competition',                 'Annual programming contest — Computer Lab 101',              '2026-03-20 13:00:00', 'EVENT',         6, CURRENT_TIMESTAMP),
(4, 'Midterm Exam — Data Structures',     'Covers chapters 1–6, bring student ID',                     '2026-03-25 09:00:00', 'EXAM',          7, CURRENT_TIMESTAMP),
(5, 'Project Submission Deadline',        'Software Engineering final project due — submit via portal', '2026-03-18 23:59:00', 'HIGH_PRIORITY', 1, CURRENT_TIMESTAMP);
