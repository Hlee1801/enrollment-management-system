package com.gcash.enrollmentmanagementsystem.util;

import com.gcash.enrollmentmanagementsystem.entity.*;
import com.gcash.enrollmentmanagementsystem.enums.DayOfWeek;
import com.gcash.enrollmentmanagementsystem.enums.EnrollmentStatus;
import com.gcash.enrollmentmanagementsystem.enums.Role;

import java.time.LocalDate;
import java.time.LocalTime;

public class TestDataBuilder {

    public static User.UserBuilder aUser() {
        return User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.STUDENT);
    }

    public static User.UserBuilder anAdmin() {
        return User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("encodedPassword")
                .role(Role.ADMIN);
    }

    public static Student.StudentBuilder aStudent() {
        return Student.builder()
                .studentNumber("2024-00001")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(2000, 1, 15));
    }

    public static Degree.DegreeBuilder aDegree() {
        return Degree.builder()
                .name("Bachelor of Science in Computer Science")
                .description("CS Degree");
    }

    public static Course.CourseBuilder aCourse() {
        return Course.builder()
                .courseCode("CS101")
                .courseName("Introduction to Programming")
                .units(3);
    }

    public static Room.RoomBuilder aRoom() {
        return Room.builder()
                .roomCode("RM-101")
                .capacity(40)
                .building("Main Building");
    }

    public static Term.TermBuilder aTerm() {
        return Term.builder()
                .termName("1st Semester 2024-2025")
                .startDate(LocalDate.of(2024, 8, 1))
                .endDate(LocalDate.of(2024, 12, 15))
                .isActive(true);
    }

    public static Schedule.ScheduleBuilder aSchedule() {
        return Schedule.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 30));
    }

    public static Schedule.ScheduleBuilder aScheduleOn(DayOfWeek day, int startHour, int endHour) {
        return Schedule.builder()
                .dayOfWeek(day)
                .startTime(LocalTime.of(startHour, 0))
                .endTime(LocalTime.of(endHour, 0));
    }

    public static Section.SectionBuilder aSection() {
        return Section.builder()
                .sectionCode("CS101-A")
                .maxSeats(40)
                .currentEnrollment(0);
    }

    public static Section.SectionBuilder aSectionWithSeats(int maxSeats, int currentEnrollment) {
        return Section.builder()
                .sectionCode("CS101-A")
                .maxSeats(maxSeats)
                .currentEnrollment(currentEnrollment);
    }

    public static Enrollment.EnrollmentBuilder anEnrollment() {
        return Enrollment.builder()
                .status(EnrollmentStatus.PENDING);
    }

    public static Enrollment.EnrollmentBuilder anEnrollmentWithStatus(EnrollmentStatus status) {
        return Enrollment.builder()
                .status(status);
    }
}
