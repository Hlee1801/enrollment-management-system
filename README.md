# Enrollment Management System

A comprehensive student enrollment management system built with Spring Boot and SvelteKit. This system allows students to enroll in courses while automatically detecting schedule conflicts and managing seat availability.

## Features

### Student Features
- Register and login with secure JWT authentication
- Browse available sections by term and course
- Enroll in sections with real-time validation
- Drop enrolled courses
- View enrollment history

### Admin Features
- View all registered students
- Manage sections (create, update, delete)
- View students enrolled in each section
- Mark enrollments as completed
- Manage academic terms

### Core Validations

| Validation | Description | HTTP Status |
|------------|-------------|-------------|
| Schedule Conflict | Prevents enrolling in sections with overlapping schedules | 409 Conflict |
| Seat Limit | Prevents enrollment when section is full | 409 Conflict |
| Degree Validation | Ensures students only enroll in courses within their degree program | 400 Bad Request |
| Duplicate Check | Prevents enrolling in the same section twice | 409 Conflict |

## Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.2.3**
- **Spring Security** with JWT Authentication
- **Spring Data JPA** with Hibernate
- **PostgreSQL** Database
- **Flyway** Database Migrations
- **Lombok** for boilerplate reduction
- **Swagger/OpenAPI** for API documentation

### Frontend
- **SvelteKit** with TypeScript
- **TailwindCSS** for styling
- **Server-side rendering** with form actions

### Testing
- **JUnit 5** for unit tests
- **Mockito** for mocking
- **Testcontainers** for integration tests
- **MockMvc** for API testing

## Database Schema

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   USERS     │     │   DEGREES   │     │    ROOMS    │
├─────────────┤     ├─────────────┤     ├─────────────┤
│ id          │     │ id          │     │ id          │
│ username    │     │ name        │     │ room_code   │
│ email       │     │ description │     │ capacity    │
│ password    │     └──────┬──────┘     │ building    │
│ role        │            │            └──────┬──────┘
└──────┬──────┘            │                   │
       │                   │ 1:N               │ 1:N
       │ 1:1               │                   │
       ▼                   ▼                   │
┌─────────────┐     ┌─────────────┐            │
│  STUDENTS   │     │   COURSES   │            │
├─────────────┤     ├─────────────┤            │
│ id          │     │ id          │            │
│ user_id     │────▶│ degree_id   │            │
│ degree_id   │     │ course_code │            │
│ student_no  │     │ course_name │            │
│ first_name  │     │ units       │            │
│ last_name   │     └──────┬──────┘            │
└──────┬──────┘            │                   │
       │                   │ 1:N               │
       │ 1:N               │                   │
       │                   ▼                   │
       │            ┌─────────────┐            │
       │            │  SECTIONS   │◀───────────┘
       │            ├─────────────┤
       │            │ id          │     ┌─────────────┐
       │            │ course_id   │     │    TERMS    │
       │            │ room_id     │     ├─────────────┤
       │            │ term_id     │◀────│ id          │
       │            │ schedule_id │     │ term_name   │
       │            │ section_code│     │ start_date  │
       │            │ max_seats   │     │ end_date    │
       │            │ current_enr │     │ is_active   │
       │            └──────┬──────┘     └─────────────┘
       │                   │
       │                   │            ┌─────────────┐
       │                   │            │  SCHEDULES  │
       │                   │            ├─────────────┤
       │                   │◀───────────│ id          │
       │                   │            │ day_of_week │
       │     N:N           │            │ start_time  │
       │   (junction)      │            │ end_time    │
       │                   │            └─────────────┘
       ▼                   ▼
┌─────────────────────────────┐
│        ENROLLMENTS          │
├─────────────────────────────┤
│ id                          │
│ student_id                  │
│ section_id                  │
│ status (PENDING/ENROLLED/   │
│         COMPLETED/DROPPED)  │
│ enrolled_at                 │
└─────────────────────────────┘
```

## Database Details

### Database Configuration

| Property | Value |
|----------|-------|
| Database | PostgreSQL 16 |
| Port | 5432 |
| Database Name | enrollment_db |
| Username | postgres |
| Password | postgres |

### Tables Specification

#### 1. USERS

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Login username |
| email | VARCHAR(100) | UNIQUE, NOT NULL | User email |
| password | VARCHAR(255) | NOT NULL | BCrypt encoded password |
| role | VARCHAR(20) | NOT NULL | STUDENT or ADMIN |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW | Account creation time |

**Indexes:** `idx_users_username`, `idx_users_email`, `idx_users_role`

#### 2. STUDENTS

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| student_number | VARCHAR(20) | UNIQUE, NOT NULL | Student ID (e.g., 2024-00001) |
| first_name | VARCHAR(50) | NOT NULL | First name |
| last_name | VARCHAR(50) | NOT NULL | Last name |
| date_of_birth | DATE | | Birth date |
| user_id | BIGINT | UNIQUE, FK → users.id | Link to user account |
| degree_id | BIGINT | FK → degrees.id | Student's degree program |

**Indexes:** `idx_students_student_number`, `idx_students_last_name`, `idx_students_user_id`

#### 3. DEGREES

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| name | VARCHAR(100) | UNIQUE, NOT NULL | Degree name |
| description | VARCHAR(500) | | Degree description |

#### 4. COURSES

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| course_code | VARCHAR(20) | UNIQUE, NOT NULL | Course code (e.g., CS101) |
| course_name | VARCHAR(100) | NOT NULL | Full course name |
| units | INTEGER | NOT NULL, CHECK (1-6) | Credit units |
| degree_id | BIGINT | NOT NULL, FK → degrees.id | Owning degree program |

#### 5. ROOMS

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| room_code | VARCHAR(20) | UNIQUE, NOT NULL | Room code (e.g., RM-101) |
| capacity | INTEGER | NOT NULL, CHECK (> 0) | Maximum capacity |
| building | VARCHAR(50) | | Building name |

#### 6. TERMS

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| term_name | VARCHAR(50) | UNIQUE, NOT NULL | Term name (e.g., 1st Semester 2024-2025) |
| start_date | DATE | NOT NULL | Term start date |
| end_date | DATE | NOT NULL, CHECK (> start_date) | Term end date |
| is_active | BOOLEAN | NOT NULL, DEFAULT FALSE | Currently active term |

#### 7. SCHEDULES

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| day_of_week | VARCHAR(20) | NOT NULL | MONDAY to SUNDAY |
| start_time | TIME | NOT NULL | Class start time |
| end_time | TIME | NOT NULL, CHECK (> start_time) | Class end time |

#### 8. SECTIONS

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| section_code | VARCHAR(20) | NOT NULL | Section code (e.g., A, B) |
| course_id | BIGINT | NOT NULL, FK → courses.id | Associated course |
| room_id | BIGINT | NOT NULL, FK → rooms.id | Assigned room |
| term_id | BIGINT | NOT NULL, FK → terms.id | Academic term |
| schedule_id | BIGINT | NOT NULL, FK → schedules.id | Class schedule |
| max_seats | INTEGER | NOT NULL, CHECK (> 0) | Maximum enrollment |
| current_enrollment | INTEGER | NOT NULL, DEFAULT 0, CHECK (>= 0, <= max_seats) | Current count |

**Indexes:** `idx_sections_course_id`, `idx_sections_term_id`, `idx_sections_section_code_term`

#### 9. ENROLLMENTS

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| student_id | BIGINT | NOT NULL, FK → students.id | Enrolling student |
| section_id | BIGINT | NOT NULL, FK → sections.id | Target section |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | PENDING, ENROLLED, COMPLETED, DROPPED |
| enrolled_at | TIMESTAMP | NOT NULL, DEFAULT NOW | Enrollment timestamp |

**Unique Constraint:** `(student_id, section_id)` - prevents duplicate enrollment

**Indexes:** `idx_enrollments_student_id`, `idx_enrollments_section_id`, `idx_enrollments_status`

### Sample Data

**Degrees:**

| ID | Name |
|----|------|
| 1 | Bachelor of Science in Computer Science |
| 2 | Bachelor of Science in Information Technology |

**Courses:**

| Code | Name | Units | Degree |
|------|------|-------|--------|
| CS101 | Introduction to Programming | 3 | Computer Science |
| CS201 | Data Structures and Algorithms | 3 | Computer Science |
| CS301 | Database Management Systems | 3 | Computer Science |
| IT101 | Fundamentals of Information Technology | 3 | Information Technology |
| IT201 | Network Administration | 3 | Information Technology |

**Rooms:**

| Code | Capacity | Building |
|------|----------|----------|
| RM-101 | 40 | Main Building |
| RM-102 | 35 | Main Building |
| RM-201 | 50 | Science Building |
| RM-202 | 30 | Science Building |
| LAB-01 | 25 | Computer Lab Building |

**Terms:**

| Name | Start | End | Active |
|------|-------|-----|--------|
| 1st Semester 2024-2025 | 2024-08-01 | 2024-12-15 | No |
| 2nd Semester 2024-2025 | 2025-01-06 | 2025-05-15 | Yes |

**Sections (Active Term):**

| Section | Course | Room | Schedule | Max Seats |
|---------|--------|------|----------|-----------|
| CS101-A | CS101 | RM-101 | Monday 08:00-09:30 | 40 |
| CS101-B | CS101 | RM-102 | Tuesday 08:00-09:30 | 35 |
| CS201-A | CS201 | RM-201 | Wednesday 10:00-11:30 | 50 |
| CS301-A | CS301 | LAB-01 | Thursday 13:00-14:30 | 25 |
| IT101-A | IT101 | RM-202 | Friday 08:00-09:30 | 30 |

### Docker Compose Configuration

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    container_name: enrollment-postgres
    environment:
      POSTGRES_DB: enrollment_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### Connecting to Database

**Using psql:**

```bash
docker exec -it enrollment-postgres psql -U postgres -d enrollment_db
```

**Using application.yml:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/enrollment_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
```

## Getting Started

### Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Node.js 18+ (for frontend)
- Maven 3.9+

### Running the Backend

1. **Start PostgreSQL with Docker:**

```bash
docker-compose up -d
```

2. **Run the Spring Boot application:**

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

3. **Access Swagger UI:**

```
http://localhost:8080/swagger-ui.html
```

### Running the Frontend

1. **Navigate to frontend directory:**

```bash
cd ../enrollment-frontend
```

2. **Install dependencies:**

```bash
npm install
```

3. **Start development server:**

```bash
npm run dev
```

The frontend will be available at `http://localhost:5173`

### Running Tests

**Run all tests:**

```bash
./mvnw test
```

**Run only unit tests:**

```bash
./mvnw test -Dtest="*Test"
```

**Run only integration tests:**

```bash
./mvnw test -Dtest="*IT"
```

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new student |
| POST | `/api/auth/login` | Login and get JWT token |
| POST | `/api/auth/refresh` | Refresh access token |
| GET | `/api/auth/degrees` | Get available degrees |

### Enrollments (Student)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/enrollments` | Get student's enrollments |
| POST | `/api/enrollments` | Create new enrollment |
| PUT | `/api/enrollments/{id}` | Update enrollment (change section) |
| DELETE | `/api/enrollments/{id}` | Drop enrollment |

### Sections (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/sections` | Get available sections |
| GET | `/api/sections/{id}` | Get section details |

### Admin

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/students` | Get all students (paginated) |
| GET | `/api/admin/sections` | Get all sections |
| POST | `/api/admin/sections` | Create new section |
| PUT | `/api/admin/sections/{id}` | Update section |
| DELETE | `/api/admin/sections/{id}` | Delete section |
| GET | `/api/admin/sections/{id}/enrollments` | Get enrollments in section |
| PUT | `/api/admin/enrollments/{id}/complete` | Mark enrollment as completed |
| GET | `/api/admin/terms` | Get all terms |
| POST | `/api/admin/terms` | Create new term |

## Business Rules Implementation

### 1. Schedule Conflict Detection

**Business Requirement:**
> "A conflict in a student's schedule is the primary cause of delay in finalizing a student's EAF (Enrollment Assessment Form). Staff members would like to inform students during enrollment (and not at the start of the term) that there are schedule conflicts."

**Solution:** Real-time schedule conflict validation during enrollment process.

**Workflow Diagram:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        SCHEDULE CONFLICT DETECTION                          │
└─────────────────────────────────────────────────────────────────────────────┘

  Student                    Frontend                     Backend
     │                          │                            │
     │  Click "Enroll"          │                            │
     │─────────────────────────>│                            │
     │                          │                            │
     │                          │  POST /api/enrollments     │
     │                          │  { sectionId: 5 }          │
     │                          │───────────────────────────>│
     │                          │                            │
     │                          │              ┌─────────────┴─────────────┐
     │                          │              │  EnrollmentService        │
     │                          │              │  .createEnrollment()      │
     │                          │              └─────────────┬─────────────┘
     │                          │                            │
     │                          │              ┌─────────────┴─────────────┐
     │                          │              │  Query existing active    │
     │                          │              │  enrollments in same term │
     │                          │              └─────────────┬─────────────┘
     │                          │                            │
     │                          │              ┌─────────────┴─────────────┐
     │                          │              │  For each enrollment:     │
     │                          │              │  Compare day_of_week      │
     │                          │              │  Compare time intervals   │
     │                          │              └─────────────┬─────────────┘
     │                          │                            │
     │                          │                     ┌──────┴──────┐
     │                          │                     │             │
     │                          │               No Conflict    Conflict Found
     │                          │                     │             │
     │                          │                     ▼             ▼
     │                          │               ┌─────────┐   ┌─────────────┐
     │                          │               │ Continue│   │ Throw       │
     │                          │               │ Process │   │ Schedule    │
     │                          │               └────┬────┘   │ Conflict    │
     │                          │                    │        │ Exception   │
     │                          │                    │        └──────┬──────┘
     │                          │                    │               │
     │                          │  201 Created       │    409 Conflict
     │                          │<───────────────────┘               │
     │                          │                                    │
     │                          │<───────────────────────────────────┘
     │  Success/Error Message   │
     │<─────────────────────────│
     │                          │
```

**Time Overlap Algorithm:**

```
Schedule A: Monday 08:00 - 10:00
Schedule B: Monday 09:00 - 11:00

Check: A.start < B.end AND B.start < A.end
       08:00 < 11:00 = TRUE
       09:00 < 10:00 = TRUE

Result: CONFLICT (both conditions are TRUE)
```

```
Schedule A: Monday 08:00 - 10:00
Schedule B: Monday 10:00 - 12:00

Check: A.start < B.end AND B.start < A.end
       08:00 < 12:00 = TRUE
       10:00 < 10:00 = FALSE

Result: NO CONFLICT (one condition is FALSE)
```

**Implementation Files:**

| File | Method | Description |
|------|--------|-------------|
| EnrollmentService.java | checkScheduleConflict() | Main validation logic |
| EnrollmentService.java | hasTimeOverlap() | Time comparison algorithm |
| EnrollmentRepository.java | findActiveEnrollmentsForScheduleConflictCheck() | Database query |
| ScheduleConflictException.java | | Custom exception class |
| GlobalExceptionHandler.java | handleScheduleConflictException() | Error response formatting |

**API Response Example:**

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Schedule conflict: Section 'CS102-A' overlaps with your enrolled section 'CS101-A'",
  "path": "/api/enrollments"
}
```

---

### 2. Seat Limit Enforcement (Overbooking Prevention)

**Business Requirement:**
> "Sections get overbooked, and some students are dropped from the said section at the start of the term. Each section has a limit on the number of seats. When this limit is reached, no more students are able to enroll."

**Solution:** Track current enrollment count and validate against maximum seats before allowing enrollment.

**Workflow Diagram:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SEAT LIMIT ENFORCEMENT                              │
└─────────────────────────────────────────────────────────────────────────────┘

                              SECTIONS Table
                    ┌────────────────────────────────┐
                    │ section_code: CS101-A          │
                    │ max_seats: 30                  │
                    │ current_enrollment: 29         │
                    └────────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                      Student Tries to Enroll                              │
└───────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │ Check: currentEnrollment      │
                    │        >= maxSeats ?          │
                    │                               │
                    │        29 >= 30 ?             │
                    │        FALSE                  │
                    └───────────────┬───────────────┘
                                    │
                           ┌────────┴────────┐
                           │                 │
                      FALSE (29<30)      TRUE (30>=30)
                           │                 │
                           ▼                 ▼
                    ┌─────────────┐   ┌─────────────────┐
                    │ ALLOW       │   │ REJECT          │
                    │ Enrollment  │   │ Throw           │
                    │             │   │ SeatLimit       │
                    │ current = 30│   │ Exceeded        │
                    └──────┬──────┘   │ Exception       │
                           │          └────────┬────────┘
                           │                   │
                           ▼                   ▼
                    ┌─────────────┐   ┌─────────────────┐
                    │ 201 Created │   │ 409 Conflict    │
                    │             │   │ "Section has    │
                    │ Enrollment  │   │  reached max    │
                    │ successful  │   │  capacity"      │
                    └─────────────┘   └─────────────────┘


                         COUNTER UPDATE FLOW

    ┌──────────────┐                         ┌──────────────┐
    │   ENROLL     │                         │    DROP      │
    └──────┬───────┘                         └──────┬───────┘
           │                                        │
           ▼                                        ▼
    ┌──────────────┐                         ┌──────────────┐
    │ current_     │                         │ current_     │
    │ enrollment   │                         │ enrollment   │
    │ += 1         │                         │ -= 1         │
    │              │                         │              │
    │ (29 -> 30)   │                         │ (30 -> 29)   │
    └──────────────┘                         └──────────────┘
```

**Implementation Files:**

| File | Method | Description |
|------|--------|-------------|
| EnrollmentService.java | createEnrollment() | Check seats before enrollment |
| EnrollmentService.java | dropEnrollment() | Decrement counter on drop |
| SeatLimitExceededException.java | | Custom exception class |
| Section.java | currentEnrollment, maxSeats | Entity fields |

**Database Constraint:**

```sql
CONSTRAINT chk_sections_enrollment_limit CHECK (current_enrollment <= max_seats)
```

**API Response Example:**

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Section 'CS101-A' has reached its maximum capacity of 30 seats",
  "path": "/api/enrollments"
}
```

---

### 3. Degree-Course Validation

**Business Requirement:**
> "A degree has a set of courses. Completing all the required courses allows the student to attain the degree."

**Solution:** Validate that the course belongs to the student's enrolled degree program before allowing enrollment.

**Workflow Diagram:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DEGREE-COURSE VALIDATION                            │
└─────────────────────────────────────────────────────────────────────────────┘

    STUDENT                                    COURSE (via Section)
    ┌─────────────────┐                       ┌─────────────────┐
    │ name: John Doe  │                       │ code: MKT101    │
    │ degree_id: 1    │                       │ name: Marketing │
    │ (Computer       │                       │ degree_id: 2    │
    │  Science)       │                       │ (Business Admin)│
    └────────┬────────┘                       └────────┬────────┘
             │                                         │
             │         ┌───────────────────┐           │
             └────────>│   COMPARISON      │<──────────┘
                       │                   │
                       │ student.degree_id │
                       │       vs          │
                       │ course.degree_id  │
                       │                   │
                       │     1 vs 2        │
                       │                   │
                       │   NOT EQUAL!      │
                       └─────────┬─────────┘
                                 │
                                 ▼
                       ┌───────────────────┐
                       │ Throw             │
                       │ CourseNotInDegree │
                       │ Exception         │
                       └─────────┬─────────┘
                                 │
                                 ▼
                       ┌───────────────────┐
                       │ 400 Bad Request   │
                       │                   │
                       │ "Course 'MKT101'  │
                       │  is not part of   │
                       │  the required     │
                       │  courses for      │
                       │  degree 'Computer │
                       │  Science'"        │
                       └───────────────────┘


                    VALID ENROLLMENT SCENARIO

    STUDENT                                    COURSE (via Section)
    ┌─────────────────┐                       ┌─────────────────┐
    │ name: John Doe  │                       │ code: CS101     │
    │ degree_id: 1    │                       │ name: Intro to  │
    │ (Computer       │                       │       Programming│
    │  Science)       │                       │ degree_id: 1    │
    └────────┬────────┘                       │ (Computer       │
             │                                │  Science)       │
             │         ┌───────────────────┐  └────────┬────────┘
             └────────>│   COMPARISON      │<──────────┘
                       │                   │
                       │     1 vs 1        │
                       │                   │
                       │     EQUAL!        │
                       └─────────┬─────────┘
                                 │
                                 ▼
                       ┌───────────────────┐
                       │ Continue to next  │
                       │ validation        │
                       │ (seat limit,      │
                       │  schedule, etc.)  │
                       └───────────────────┘
```

**Data Relationship:**

```
DEGREES
┌────┬───────────────────────────────────────┐
│ ID │ NAME                                  │
├────┼───────────────────────────────────────┤
│ 1  │ Bachelor of Science in Computer Science│
│ 2  │ Bachelor of Science in Business Admin │
└────┴───────────────────────────────────────┘
         │                    │
         │ 1:N                │ 1:N
         ▼                    ▼
COURSES                    COURSES
┌────────┬─────────────┐   ┌────────┬─────────────┐
│ CS101  │ degree_id=1 │   │ BA101  │ degree_id=2 │
│ CS201  │ degree_id=1 │   │ MKT101 │ degree_id=2 │
│ CS301  │ degree_id=1 │   │ FIN101 │ degree_id=2 │
└────────┴─────────────┘   └────────┴─────────────┘

STUDENTS
┌──────────────┬─────────────┬─────────────────────────┐
│ Student      │ degree_id   │ Can Enroll In           │
├──────────────┼─────────────┼─────────────────────────┤
│ John Doe     │ 1           │ CS101, CS201, CS301     │
│ Jane Smith   │ 2           │ BA101, MKT101, FIN101   │
└──────────────┴─────────────┴─────────────────────────┘
```

**Implementation Files:**

| File | Method | Description |
|------|--------|-------------|
| EnrollmentService.java | checkCourseInDegree() | Validation logic |
| CourseNotInDegreeException.java | | Custom exception class |
| Student.java | degree (ManyToOne) | Student's degree program |
| Course.java | degree (ManyToOne) | Course's owning degree |

---

### 4. Complete Enrollment Validation Flow

**Full Workflow Diagram:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    COMPLETE ENROLLMENT VALIDATION FLOW                       │
└─────────────────────────────────────────────────────────────────────────────┘

Student clicks "Enroll" on Section
                │
                ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ POST /api/enrollments { sectionId: 5 }                                    │
└───────────────────────────────────────────────────────────────────────────┘
                │
                ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ STEP 1: Authentication Check                                              │
│ Is user authenticated with valid JWT token?                               │
└───────────────────────────────────────────────────────────────────────────┘
                │
        ┌───────┴───────┐
        │               │
       YES              NO ──────────────> 401 Unauthorized
        │
        ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ STEP 2: Get Section                                                       │
│ Does section exist in database?                                           │
└───────────────────────────────────────────────────────────────────────────┘
                │
        ┌───────┴───────┐
        │               │
       YES              NO ──────────────> 404 Not Found
        │                                  "Section not found"
        ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ STEP 3: Degree Validation                                                 │
│ Does course belong to student's degree?                                   │
│ course.degree.id == student.degree.id ?                                   │
└───────────────────────────────────────────────────────────────────────────┘
                │
        ┌───────┴───────┐
        │               │
       YES              NO ──────────────> 400 Bad Request
        │                                  "Course not in degree"
        ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ STEP 4: Duplicate Check                                                   │
│ Is student already enrolled in this section?                              │
│ existsByStudentIdAndSectionId() ?                                         │
└───────────────────────────────────────────────────────────────────────────┘
                │
        ┌───────┴───────┐
        │               │
        NO             YES ──────────────> 409 Conflict
        │                                  "Already enrolled"
        ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ STEP 5: Seat Limit Check                                                  │
│ Are seats available?                                                      │
│ currentEnrollment < maxSeats ?                                            │
└───────────────────────────────────────────────────────────────────────────┘
                │
        ┌───────┴───────┐
        │               │
       YES              NO ──────────────> 409 Conflict
        │                                  "Section full"
        ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ STEP 6: Schedule Conflict Check                                           │
│ Does new section overlap with existing enrollments?                       │
│ Check all active enrollments in same term                                 │
└───────────────────────────────────────────────────────────────────────────┘
                │
        ┌───────┴───────┐
        │               │
        NO             YES ──────────────> 409 Conflict
        │                                  "Schedule conflict"
        ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ STEP 7: Create Enrollment                                                 │
│ Save enrollment record with status = ENROLLED                             │
│ Increment section.currentEnrollment                                       │
└───────────────────────────────────────────────────────────────────────────┘
                │
                ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ 201 Created                                                               │
│ Return EnrollmentDto with student, section, status, enrolledAt            │
└───────────────────────────────────────────────────────────────────────────┘
```

**Validation Order Summary:**

| Step | Validation | Exception | HTTP Status |
|------|------------|-----------|-------------|
| 1 | JWT Authentication | AuthenticationException | 401 |
| 2 | Section Exists | ResourceNotFoundException | 404 |
| 3 | Course in Degree | CourseNotInDegreeException | 400 |
| 4 | Not Duplicate | DuplicateEnrollmentException | 409 |
| 5 | Seats Available | SeatLimitExceededException | 409 |
| 6 | No Schedule Conflict | ScheduleConflictException | 409 |
| 7 | Create Enrollment | (Success) | 201 |

**Code Implementation:**

```java
public EnrollmentDto createEnrollment(EnrollmentCreateRequest request) {
    // Step 1: Get authenticated student (handled by Spring Security)
    Student student = studentService.getCurrentStudent();

    // Step 2: Get section (throws 404 if not found)
    Section section = sectionRepository.findById(request.getSectionId())
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));

    // Step 3: Validate degree
    checkCourseInDegree(student, section);

    // Step 4: Check duplicate
    if (enrollmentRepository.existsByStudentIdAndSectionId(student.getId(), section.getId())) {
        throw new DuplicateEnrollmentException(student.getStudentNumber(), section.getSectionCode());
    }

    // Step 5: Check seat limit
    if (section.getCurrentEnrollment() >= section.getMaxSeats()) {
        throw new SeatLimitExceededException(section.getSectionCode(), section.getMaxSeats());
    }

    // Step 6: Check schedule conflict
    checkScheduleConflict(student, section);

    // Step 7: Create enrollment
    Enrollment enrollment = Enrollment.builder()
            .student(student)
            .section(section)
            .status(EnrollmentStatus.ENROLLED)
            .build();

    enrollment = enrollmentRepository.save(enrollment);
    section.setCurrentEnrollment(section.getCurrentEnrollment() + 1);
    sectionRepository.save(section);

    return toDto(enrollment);
}
```



## Default Credentials

After running migrations, the following accounts are available:

| Username | Password | Role |
|----------|----------|------|
| admin1 | admin123 | ADMIN |
| jdoe | student123 | STUDENT |

## License

This project is developed for educational purposes.

## Author

Developed as part of the GCash Machine Problem assignment.
