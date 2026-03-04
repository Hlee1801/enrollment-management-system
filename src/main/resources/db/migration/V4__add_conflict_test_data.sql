-- V4: Add test data for Schedule Conflict scenarios

-- MONDAY schedules (to test overlap with CS101-A: Mon 08:00-09:30)
INSERT INTO schedules (day_of_week, start_time, end_time) VALUES
('MONDAY', '09:00:00', '10:30:00'),
('MONDAY', '07:30:00', '09:00:00'),
('MONDAY', '08:30:00', '09:00:00'),
('MONDAY', '07:00:00', '10:00:00'),
('MONDAY', '09:30:00', '11:00:00');

-- WEDNESDAY schedules (to test overlap with CS201-A: Wed 10:00-11:30)
INSERT INTO schedules (day_of_week, start_time, end_time) VALUES
('WEDNESDAY', '11:00:00', '12:30:00'),
('WEDNESDAY', '09:00:00', '10:30:00'),
('WEDNESDAY', '10:30:00', '11:00:00'),
('WEDNESDAY', '09:30:00', '12:00:00'),
('WEDNESDAY', '11:30:00', '13:00:00');

-- Additional Courses
INSERT INTO courses (course_code, course_name, units, degree_id) VALUES
('CS401', 'Software Engineering', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Computer Science')),
('CS402', 'Operating Systems', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Computer Science')),
('CS403', 'Computer Networks', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Computer Science')),
('CS404', 'Artificial Intelligence', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Computer Science')),
('CS405', 'Web Development', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Computer Science')),
('IT301', 'System Administration', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Information Technology')),
('IT302', 'Cybersecurity Fundamentals', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Information Technology')),
('IT303', 'Cloud Computing', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Information Technology')),
('IT304', 'IT Project Management', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Information Technology')),
('IT305', 'Database Administration', 3, (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Information Technology'));

-- Sections for MONDAY Conflict Testing (conflict with CS101-A Mon 08:00-09:30)
INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('CS401-A',
    (SELECT id FROM courses WHERE course_code = 'CS401'),
    (SELECT id FROM rooms WHERE room_code = 'RM-201'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'MONDAY' AND start_time = '08:00:00' AND end_time = '09:30:00'),
    30, 0);

INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('CS402-A',
    (SELECT id FROM courses WHERE course_code = 'CS402'),
    (SELECT id FROM rooms WHERE room_code = 'RM-201'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'MONDAY' AND start_time = '09:00:00' AND end_time = '10:30:00'),
    30, 0);

INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('CS403-A',
    (SELECT id FROM courses WHERE course_code = 'CS403'),
    (SELECT id FROM rooms WHERE room_code = 'RM-201'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'MONDAY' AND start_time = '07:30:00' AND end_time = '09:00:00'),
    30, 0);

INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('CS404-A',
    (SELECT id FROM courses WHERE course_code = 'CS404'),
    (SELECT id FROM rooms WHERE room_code = 'RM-201'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'MONDAY' AND start_time = '08:30:00' AND end_time = '09:00:00'),
    30, 0);

INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('CS405-A',
    (SELECT id FROM courses WHERE course_code = 'CS405'),
    (SELECT id FROM rooms WHERE room_code = 'RM-201'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'MONDAY' AND start_time = '07:00:00' AND end_time = '10:00:00'),
    30, 0);

-- Adjacent section - NO CONFLICT
INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('IT301-A',
    (SELECT id FROM courses WHERE course_code = 'IT301'),
    (SELECT id FROM rooms WHERE room_code = 'RM-201'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'MONDAY' AND start_time = '09:30:00' AND end_time = '11:00:00'),
    30, 0);

-- Sections for WEDNESDAY Conflict Testing (conflict with CS201-A Wed 10:00-11:30)
INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('IT302-A',
    (SELECT id FROM courses WHERE course_code = 'IT302'),
    (SELECT id FROM rooms WHERE room_code = 'RM-202'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'WEDNESDAY' AND start_time = '11:00:00' AND end_time = '12:30:00'),
    30, 0);

INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('IT303-A',
    (SELECT id FROM courses WHERE course_code = 'IT303'),
    (SELECT id FROM rooms WHERE room_code = 'RM-202'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'WEDNESDAY' AND start_time = '09:00:00' AND end_time = '10:30:00'),
    30, 0);

INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('IT304-A',
    (SELECT id FROM courses WHERE course_code = 'IT304'),
    (SELECT id FROM rooms WHERE room_code = 'RM-202'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'WEDNESDAY' AND start_time = '10:30:00' AND end_time = '11:00:00'),
    30, 0);

INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('IT305-A',
    (SELECT id FROM courses WHERE course_code = 'IT305'),
    (SELECT id FROM rooms WHERE room_code = 'RM-202'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'WEDNESDAY' AND start_time = '09:30:00' AND end_time = '12:00:00'),
    30, 0);

-- Adjacent section - NO CONFLICT
INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('CS401-B',
    (SELECT id FROM courses WHERE course_code = 'CS401'),
    (SELECT id FROM rooms WHERE room_code = 'RM-202'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'WEDNESDAY' AND start_time = '11:30:00' AND end_time = '13:00:00'),
    30, 0);

-- Sections on DIFFERENT DAYS - NO CONFLICT
INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('CS402-B',
    (SELECT id FROM courses WHERE course_code = 'CS402'),
    (SELECT id FROM rooms WHERE room_code = 'RM-101'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'TUESDAY' AND start_time = '08:00:00'),
    35, 0);

INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('CS403-B',
    (SELECT id FROM courses WHERE course_code = 'CS403'),
    (SELECT id FROM rooms WHERE room_code = 'RM-101'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'THURSDAY' AND start_time = '08:00:00'),
    35, 0);

-- Section in DIFFERENT TERM - NO CONFLICT
INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('CS401-OLD',
    (SELECT id FROM courses WHERE course_code = 'CS401'),
    (SELECT id FROM rooms WHERE room_code = 'RM-101'),
    (SELECT id FROM terms WHERE term_name = '1st Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'MONDAY' AND start_time = '08:00:00' AND end_time = '09:30:00'),
    30, 0);

-- Section with FULL SEATS (max 2, will have 2 enrolled)
INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('CS404-FULL',
    (SELECT id FROM courses WHERE course_code = 'CS404'),
    (SELECT id FROM rooms WHERE room_code = 'LAB-01'),
    (SELECT id FROM terms WHERE term_name = '2nd Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'FRIDAY' AND start_time = '10:00:00'),
    2, 0);

-- Enrollments to fill CS404-FULL section
INSERT INTO enrollments (student_id, section_id, status, enrolled_at) VALUES
((SELECT id FROM students WHERE student_number = '2024-00004'),
 (SELECT id FROM sections WHERE section_code = 'CS404-FULL'),
 'ENROLLED', CURRENT_TIMESTAMP),
((SELECT id FROM students WHERE student_number = '2024-00005'),
 (SELECT id FROM sections WHERE section_code = 'CS404-FULL'),
 'ENROLLED', CURRENT_TIMESTAMP);

-- Additional Student for Testing
INSERT INTO users (username, email, password, role, created_at) VALUES
('testuser', 'test@obu.edu', '$2a$10$EqKcp1WFKzJpS9YFpJTLMOpMVFoR3pXE1fV3RLgqKL3jPd5I4cIwC', 'STUDENT', CURRENT_TIMESTAMP);

INSERT INTO students (student_number, first_name, last_name, date_of_birth, user_id) VALUES
('2024-00006', 'Test', 'User', '2000-06-15', (SELECT id FROM users WHERE username = 'testuser'));

-- Enrollment with DROPPED status
INSERT INTO enrollments (student_id, section_id, status, enrolled_at) VALUES
((SELECT id FROM students WHERE student_number = '2024-00006'),
 (SELECT id FROM sections WHERE section_code = 'CS101-B'),
 'DROPPED', CURRENT_TIMESTAMP);

-- Update enrollment count
UPDATE sections SET current_enrollment = (
    SELECT COUNT(*) FROM enrollments
    WHERE enrollments.section_id = sections.id
    AND enrollments.status IN ('PENDING', 'ENROLLED')
);
