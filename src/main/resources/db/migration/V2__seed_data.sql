-- =============================================
-- Enrollment Management System - Seed Data
-- =============================================

-- =============================================
-- Admin Users (password: admin123 - BCrypt encoded)
-- =============================================
INSERT INTO users (username, email, password, role, created_at) VALUES
('admin1', 'admin1@obu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQWJZJPVrUlPd0DJxHIjjEv7eWLi', 'ADMIN', CURRENT_TIMESTAMP),
('admin2', 'admin2@obu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQWJZJPVrUlPd0DJxHIjjEv7eWLi', 'ADMIN', CURRENT_TIMESTAMP);

-- =============================================
-- Student Users (password: student123 - BCrypt encoded)
-- =============================================
INSERT INTO users (username, email, password, role, created_at) VALUES
('jdoe', 'john.doe@obu.edu', '$2a$10$EqKcp1WFKzJpS9YFpJTLMOpMVFoR3pXE1fV3RLgqKL3jPd5I4cIwC', 'STUDENT', CURRENT_TIMESTAMP),
('jsmith', 'jane.smith@obu.edu', '$2a$10$EqKcp1WFKzJpS9YFpJTLMOpMVFoR3pXE1fV3RLgqKL3jPd5I4cIwC', 'STUDENT', CURRENT_TIMESTAMP),
('mjohnson', 'mike.johnson@obu.edu', '$2a$10$EqKcp1WFKzJpS9YFpJTLMOpMVFoR3pXE1fV3RLgqKL3jPd5I4cIwC', 'STUDENT', CURRENT_TIMESTAMP),
('ewilliams', 'emily.williams@obu.edu', '$2a$10$EqKcp1WFKzJpS9YFpJTLMOpMVFoR3pXE1fV3RLgqKL3jPd5I4cIwC', 'STUDENT', CURRENT_TIMESTAMP),
('dbrown', 'david.brown@obu.edu', '$2a$10$EqKcp1WFKzJpS9YFpJTLMOpMVFoR3pXE1fV3RLgqKL3jPd5I4cIwC', 'STUDENT', CURRENT_TIMESTAMP);

-- =============================================
-- Students
-- =============================================
INSERT INTO students (student_number, first_name, last_name, date_of_birth, user_id) VALUES
('2024-00001', 'John', 'Doe', '2000-05-15', (SELECT id FROM users WHERE username = 'jdoe')),
('2024-00002', 'Jane', 'Smith', '2001-03-22', (SELECT id FROM users WHERE username = 'jsmith')),
('2024-00003', 'Mike', 'Johnson', '2000-11-08', (SELECT id FROM users WHERE username = 'mjohnson')),
('2024-00004', 'Emily', 'Williams', '2001-07-30', (SELECT id FROM users WHERE username = 'ewilliams')),
('2024-00005', 'David', 'Brown', '2000-01-12', (SELECT id FROM users WHERE username = 'dbrown'));

-- =============================================
-- Degrees
-- =============================================
INSERT INTO degrees (name, description) VALUES
('Bachelor of Science in Computer Science', 'A comprehensive program covering software development, algorithms, data structures, and computer systems.'),
('Bachelor of Science in Information Technology', 'A program focused on information systems, networking, and technology management.');

-- =============================================
-- Courses
-- =============================================
INSERT INTO courses (course_code, course_name, units, degree_id) VALUES
('CS101', 'Introduction to Programming', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Computer Science')),
('CS201', 'Data Structures and Algorithms', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Computer Science')),
('CS301', 'Database Management Systems', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Computer Science')),
('IT101', 'Fundamentals of Information Technology', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Information Technology')),
('IT201', 'Network Administration', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Information Technology'));

-- =============================================
-- Rooms
-- =============================================
INSERT INTO rooms (room_code, capacity, building) VALUES
('RM-101', 40, 'Main Building'),
('RM-102', 35, 'Main Building'),
('RM-201', 50, 'Science Building'),
('RM-202', 30, 'Science Building'),
('LAB-01', 25, 'Computer Lab Building');

-- =============================================
-- Terms
-- =============================================
INSERT INTO terms (term_name, start_date, end_date, is_active) VALUES
('1st Semester 2024-2025', '2024-08-01', '2024-12-15', FALSE),
('2nd Semester 2024-2025', '2025-01-06', '2025-05-15', TRUE);

-- =============================================
-- Schedules
-- =============================================
INSERT INTO schedules (day_of_week, start_time, end_time) VALUES
('MONDAY', '08:00:00', '09:30:00'),
('MONDAY', '10:00:00', '11:30:00'),
('TUESDAY', '08:00:00', '09:30:00'),
('TUESDAY', '13:00:00', '14:30:00'),
('WEDNESDAY', '08:00:00', '09:30:00'),
('WEDNESDAY', '10:00:00', '11:30:00'),
('THURSDAY', '08:00:00', '09:30:00'),
('THURSDAY', '13:00:00', '14:30:00'),
('FRIDAY', '08:00:00', '09:30:00'),
('FRIDAY', '10:00:00', '11:30:00');

-- =============================================
-- Sections (5 sections for current active term)
-- =============================================
INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
-- CS101 Sections
('CS101-A',
    (SELECT id FROM courses WHERE course_code = 'CS101'),
    (SELECT id FROM rooms WHERE room_code = 'RM-101'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'MONDAY' AND start_time = '08:00:00'),
    40, 0),
('CS101-B',
    (SELECT id FROM courses WHERE course_code = 'CS101'),
    (SELECT id FROM rooms WHERE room_code = 'RM-102'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'TUESDAY' AND start_time = '08:00:00'),
    35, 0),
-- CS201 Section
('CS201-A',
    (SELECT id FROM courses WHERE course_code = 'CS201'),
    (SELECT id FROM rooms WHERE room_code = 'RM-201'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'WEDNESDAY' AND start_time = '10:00:00'),
    50, 0),
-- CS301 Section
('CS301-A',
    (SELECT id FROM courses WHERE course_code = 'CS301'),
    (SELECT id FROM rooms WHERE room_code = 'LAB-01'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'THURSDAY' AND start_time = '13:00:00'),
    25, 0),
-- IT101 Section
('IT101-A',
    (SELECT id FROM courses WHERE course_code = 'IT101'),
    (SELECT id FROM rooms WHERE room_code = 'RM-202'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'FRIDAY' AND start_time = '08:00:00'),
    30, 0);

-- =============================================
-- Sample Enrollments
-- =============================================
INSERT INTO enrollments (student_id, section_id, status, enrolled_at) VALUES
-- John Doe enrollments
((SELECT id FROM students WHERE student_number = '2024-00001'),
 (SELECT id FROM sections WHERE section_code = 'CS101-A'),
 'ENROLLED', CURRENT_TIMESTAMP),
((SELECT id FROM students WHERE student_number = '2024-00001'),
 (SELECT id FROM sections WHERE section_code = 'CS201-A'),
 'ENROLLED', CURRENT_TIMESTAMP),
-- Jane Smith enrollments
((SELECT id FROM students WHERE student_number = '2024-00002'),
 (SELECT id FROM sections WHERE section_code = 'CS101-B'),
 'ENROLLED', CURRENT_TIMESTAMP),
((SELECT id FROM students WHERE student_number = '2024-00002'),
 (SELECT id FROM sections WHERE section_code = 'CS301-A'),
 'PENDING', CURRENT_TIMESTAMP),
-- Mike Johnson enrollment
((SELECT id FROM students WHERE student_number = '2024-00003'),
 (SELECT id FROM sections WHERE section_code = 'IT101-A'),
 'ENROLLED', CURRENT_TIMESTAMP);

-- =============================================
-- Update current_enrollment counts
-- =============================================
UPDATE sections SET current_enrollment = (
    SELECT COUNT(*) FROM enrollments
    WHERE enrollments.section_id = sections.id
    AND enrollments.status IN ('PENDING', 'ENROLLED')
);
