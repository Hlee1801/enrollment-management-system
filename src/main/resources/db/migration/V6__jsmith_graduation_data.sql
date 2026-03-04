-- V6: Add data for jsmith to complete all BSCS courses and graduate

-- Create sections in previous term (1st Semester) for completed courses
INSERT INTO sections (section_code, course_id, room_id, term_id, schedule_id, max_seats, current_enrollment) VALUES
('CS101-OLD',
    (SELECT id FROM courses WHERE course_code = 'CS101'),
    (SELECT id FROM rooms WHERE room_code = 'RM-101'),
    (SELECT id FROM terms WHERE term_name = '1st Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'MONDAY' AND start_time = '08:00:00' AND end_time = '09:30:00'),
    40, 1),
('CS201-OLD',
    (SELECT id FROM courses WHERE course_code = 'CS201'),
    (SELECT id FROM rooms WHERE room_code = 'RM-201'),
    (SELECT id FROM terms WHERE term_name = '1st Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'WEDNESDAY' AND start_time = '10:00:00' AND end_time = '11:30:00'),
    50, 1),
('CS301-OLD',
    (SELECT id FROM courses WHERE course_code = 'CS301'),
    (SELECT id FROM rooms WHERE room_code = 'LAB-01'),
    (SELECT id FROM terms WHERE term_name = '1st Semester 2024-2025'),
    (SELECT id FROM schedules WHERE day_of_week = 'THURSDAY' AND start_time = '13:00:00' AND end_time = '14:30:00'),
    25, 1);

-- Delete jsmith's current enrollments (will be replaced with COMPLETED ones)
DELETE FROM enrollments WHERE student_id = (SELECT id FROM students WHERE student_number = '2024-00002');

-- Create COMPLETED enrollments for all BSCS courses for jsmith
INSERT INTO enrollments (student_id, section_id, status, enrolled_at) VALUES
-- Core courses (from V2)
((SELECT id FROM students WHERE student_number = '2024-00002'),
 (SELECT id FROM sections WHERE section_code = 'CS101-OLD'),
 'COMPLETED', '2024-12-10 10:00:00'),
((SELECT id FROM students WHERE student_number = '2024-00002'),
 (SELECT id FROM sections WHERE section_code = 'CS201-OLD'),
 'COMPLETED', '2024-12-10 10:00:00'),
((SELECT id FROM students WHERE student_number = '2024-00002'),
 (SELECT id FROM sections WHERE section_code = 'CS301-OLD'),
 'COMPLETED', '2024-12-10 10:00:00');

-- Update sections enrollment counts
UPDATE sections SET current_enrollment = (
    SELECT COUNT(*) FROM enrollments
    WHERE enrollments.section_id = sections.id
    AND enrollments.status IN ('PENDING', 'ENROLLED')
);

-- Ensure jsmith has BSCS degree assigned
UPDATE students
SET degree_id = (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Computer Science')
WHERE student_number = '2024-00002';
