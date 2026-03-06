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

The system prevents students from enrolling in sections that have overlapping schedules within the same term.

**Algorithm:**
```
For each existing enrollment in the same term:
    If same day of week:
        If (new.startTime < existing.endTime) AND (existing.startTime < new.endTime):
            CONFLICT DETECTED
```

**Location:** `EnrollmentService.checkScheduleConflict()`

### 2. Seat Limit Enforcement

Each section has a maximum capacity. The system tracks current enrollment and prevents overbooking.

**Logic:**
```
If currentEnrollment >= maxSeats:
    Throw SeatLimitExceededException
Else:
    Allow enrollment and increment counter
```

**Location:** `EnrollmentService.createEnrollment()`

### 3. Degree-Course Validation

Students can only enroll in courses that belong to their degree program.

**Logic:**
```
If course.degree.id != student.degree.id:
    Throw CourseNotInDegreeException
```

**Location:** `EnrollmentService.checkCourseInDegree()`

## Project Structure

```
src/
├── main/
│   ├── java/com/gcash/enrollmentmanagementsystem/
│   │   ├── config/          # Security, Swagger configs
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── enums/           # Enums (Role, Status, DayOfWeek)
│   │   ├── exception/       # Custom exceptions
│   │   ├── repository/      # Spring Data repositories
│   │   ├── security/        # JWT provider, filters
│   │   └── service/         # Business logic
│   └── resources/
│       ├── db/migration/    # Flyway migrations
│       └── application.yml  # Configuration
└── test/
    └── java/com/gcash/enrollmentmanagementsystem/
        ├── controller/      # Integration tests (*IT.java)
        ├── service/         # Unit tests (*Test.java)
        └── security/        # Security tests
```

## Enrollment Status Lifecycle

```
    ┌──────────┐
    │ PENDING  │ (Initial state when enrolling)
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │ ENROLLED │ (Student confirmed in section)
    └────┬─────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌──────────┐  ┌──────────┐
│ COMPLETED│  │ DROPPED  │
│ (Passed) │  │ (Student │
└──────────┘  │  dropped)│
              └──────────┘
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
