-- V5: Add degree progress tracking

-- Add degree_id column to students table
ALTER TABLE students ADD COLUMN degree_id BIGINT;

ALTER TABLE students
    ADD CONSTRAINT fk_students_degree FOREIGN KEY (degree_id) REFERENCES degrees(id);

CREATE INDEX idx_students_degree_id ON students(degree_id);

-- Update enrollment status CHECK constraint to include COMPLETED
ALTER TABLE enrollments DROP CONSTRAINT chk_enrollments_status;
ALTER TABLE enrollments ADD CONSTRAINT chk_enrollments_status
    CHECK (status IN ('PENDING', 'ENROLLED', 'DROPPED', 'COMPLETED'));

-- Assign degrees to existing students based on their enrollments
UPDATE students s
SET degree_id = (
    SELECT DISTINCT c.degree_id
    FROM enrollments e
    JOIN sections sec ON e.section_id = sec.id
    JOIN courses c ON sec.course_id = c.id
    WHERE e.student_id = s.id
    AND e.status IN ('ENROLLED', 'COMPLETED')
    LIMIT 1
)
WHERE degree_id IS NULL;

-- For students without enrollments, assign default degree (BSCS)
UPDATE students
SET degree_id = (SELECT id FROM degrees WHERE name = 'Bachelor of Science in Computer Science')
WHERE degree_id IS NULL;
