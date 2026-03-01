-- =============================================
-- Enrollment Management System - Initial Schema
-- =============================================

-- Create ENUM types
CREATE TYPE role_type AS ENUM ('STUDENT', 'ADMIN');
CREATE TYPE day_of_week_type AS ENUM ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY');
CREATE TYPE enrollment_status_type AS ENUM ('PENDING', 'ENROLLED', 'DROPPED');

-- =============================================
-- Users Table
-- =============================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('STUDENT', 'ADMIN'))
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- =============================================
-- Students Table
-- =============================================
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    student_number VARCHAR(20) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE,
    user_id BIGINT NOT NULL,

    CONSTRAINT uk_students_student_number UNIQUE (student_number),
    CONSTRAINT uk_students_user_id UNIQUE (user_id),
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_students_student_number ON students(student_number);
CREATE INDEX idx_students_last_name ON students(last_name);
CREATE INDEX idx_students_user_id ON students(user_id);

-- =============================================
-- Degrees Table
-- =============================================
CREATE TABLE degrees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),

    CONSTRAINT uk_degrees_name UNIQUE (name)
);

CREATE INDEX idx_degrees_name ON degrees(name);

-- =============================================
-- Courses Table
-- =============================================
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    units INTEGER NOT NULL,
    degree_id BIGINT NOT NULL,

    CONSTRAINT uk_courses_course_code UNIQUE (course_code),
    CONSTRAINT fk_courses_degree FOREIGN KEY (degree_id) REFERENCES degrees(id) ON DELETE CASCADE,
    CONSTRAINT chk_courses_units CHECK (units > 0 AND units <= 6)
);

CREATE INDEX idx_courses_course_code ON courses(course_code);
CREATE INDEX idx_courses_degree_id ON courses(degree_id);

-- =============================================
-- Rooms Table
-- =============================================
CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    room_code VARCHAR(20) NOT NULL,
    capacity INTEGER NOT NULL,
    building VARCHAR(50),

    CONSTRAINT uk_rooms_room_code UNIQUE (room_code),
    CONSTRAINT chk_rooms_capacity CHECK (capacity > 0)
);

CREATE INDEX idx_rooms_room_code ON rooms(room_code);
CREATE INDEX idx_rooms_building ON rooms(building);

-- =============================================
-- Terms Table
-- =============================================
CREATE TABLE terms (
    id BIGSERIAL PRIMARY KEY,
    term_name VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_terms_term_name UNIQUE (term_name),
    CONSTRAINT chk_terms_dates CHECK (end_date > start_date)
);

CREATE INDEX idx_terms_term_name ON terms(term_name);
CREATE INDEX idx_terms_is_active ON terms(is_active);

-- =============================================
-- Schedules Table
-- =============================================
CREATE TABLE schedules (
    id BIGSERIAL PRIMARY KEY,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    CONSTRAINT chk_schedules_day_of_week CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    CONSTRAINT chk_schedules_times CHECK (end_time > start_time)
);

CREATE INDEX idx_schedules_day_of_week ON schedules(day_of_week);

-- =============================================
-- Sections Table
-- =============================================
CREATE TABLE sections (
    id BIGSERIAL PRIMARY KEY,
    section_code VARCHAR(20) NOT NULL,
    course_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    term_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    max_seats INTEGER NOT NULL,
    current_enrollment INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_sections_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_sections_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_sections_term FOREIGN KEY (term_id) REFERENCES terms(id) ON DELETE CASCADE,
    CONSTRAINT fk_sections_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT chk_sections_max_seats CHECK (max_seats > 0),
    CONSTRAINT chk_sections_current_enrollment CHECK (current_enrollment >= 0),
    CONSTRAINT chk_sections_enrollment_limit CHECK (current_enrollment <= max_seats)
);

CREATE INDEX idx_sections_course_id ON sections(course_id);
CREATE INDEX idx_sections_room_id ON sections(room_id);
CREATE INDEX idx_sections_term_id ON sections(term_id);
CREATE INDEX idx_sections_schedule_id ON sections(schedule_id);
CREATE INDEX idx_sections_section_code_term ON sections(section_code, term_id);

-- =============================================
-- Enrollments Table
-- =============================================
CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    section_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_enrollments_student_section UNIQUE (student_id, section_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_section FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE,
    CONSTRAINT chk_enrollments_status CHECK (status IN ('PENDING', 'ENROLLED', 'DROPPED'))
);

CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_section_id ON enrollments(section_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE INDEX idx_enrollments_enrolled_at ON enrollments(enrolled_at);
