package com.gcash.enrollmentmanagementsystem.service;

import com.gcash.enrollmentmanagementsystem.dto.EnrollmentCreateRequest;
import com.gcash.enrollmentmanagementsystem.dto.EnrollmentDto;
import com.gcash.enrollmentmanagementsystem.entity.*;
import com.gcash.enrollmentmanagementsystem.enums.DayOfWeek;
import com.gcash.enrollmentmanagementsystem.enums.EnrollmentStatus;
import com.gcash.enrollmentmanagementsystem.exception.*;
import com.gcash.enrollmentmanagementsystem.repository.EnrollmentRepository;
import com.gcash.enrollmentmanagementsystem.repository.SectionRepository;
import com.gcash.enrollmentmanagementsystem.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class
EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student student;
    private Section section;
    private Term term;
    private Schedule schedule;
    private Course course;
    private Degree degree;
    private Room room;

    @BeforeEach
    void setUp() {
        User user = TestDataBuilder.aUser().id(1L).build();
        student = TestDataBuilder.aStudent().id(1L).user(user).build();
        degree = TestDataBuilder.aDegree().id(1L).build();
        course = TestDataBuilder.aCourse().id(1L).degree(degree).build();
        room = TestDataBuilder.aRoom().id(1L).build();
        term = TestDataBuilder.aTerm().id(1L).build();
        schedule = TestDataBuilder.aSchedule().id(1L).build();
        section = TestDataBuilder.aSection()
                .id(1L)
                .course(course)
                .room(room)
                .term(term)
                .schedule(schedule)
                .build();
    }

    @Nested
    @DisplayName("Enroll Student Tests")
    class EnrollStudentTests {

        @Test
        @DisplayName("Should enroll student successfully")
        void testEnrollStudent_Success() {
            // Given
            EnrollmentCreateRequest request = new EnrollmentCreateRequest(1L);
            Enrollment savedEnrollment = TestDataBuilder.anEnrollment()
                    .id(1L)
                    .student(student)
                    .section(section)
                    .build();

            when(studentService.getCurrentStudent()).thenReturn(student);
            when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
            when(enrollmentRepository.existsByStudentIdAndSectionId(anyLong(), anyLong())).thenReturn(false);
            when(enrollmentRepository.findActiveEnrollmentsForScheduleConflictCheck(anyLong(), anyLong()))
                    .thenReturn(List.of());
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(savedEnrollment);
            when(sectionRepository.save(any(Section.class))).thenReturn(section);
            when(studentService.toDto(any(Student.class))).thenReturn(null);

            // When
            EnrollmentDto result = enrollmentService.createEnrollment(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
            verify(enrollmentRepository).save(any(Enrollment.class));
            verify(sectionRepository).save(any(Section.class));
        }

        @ParameterizedTest(name = "maxSeats={0}, currentEnrollment={1} should throw SeatLimitExceededException")
        @CsvSource({
                "30, 30",
                "40, 40",
                "25, 25",
                "10, 15"
        })
        @DisplayName("Should throw SeatLimitExceededException when section is full")
        void testEnrollStudent_SeatLimitExceeded(int maxSeats, int currentEnrollment) {
            // Given
            Section fullSection = TestDataBuilder.aSectionWithSeats(maxSeats, currentEnrollment)
                    .id(1L)
                    .course(course)
                    .room(room)
                    .term(term)
                    .schedule(schedule)
                    .build();

            EnrollmentCreateRequest request = new EnrollmentCreateRequest(1L);

            when(studentService.getCurrentStudent()).thenReturn(student);
            when(sectionRepository.findById(1L)).thenReturn(Optional.of(fullSection));
            when(enrollmentRepository.existsByStudentIdAndSectionId(anyLong(), anyLong())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> enrollmentService.createEnrollment(request))
                    .isInstanceOf(SeatLimitExceededException.class);

            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should throw DuplicateEnrollmentException when already enrolled")
        void testEnrollStudent_DuplicateEnrollment() {
            // Given
            EnrollmentCreateRequest request = new EnrollmentCreateRequest(1L);

            when(studentService.getCurrentStudent()).thenReturn(student);
            when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
            when(enrollmentRepository.existsByStudentIdAndSectionId(anyLong(), anyLong())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> enrollmentService.createEnrollment(request))
                    .isInstanceOf(DuplicateEnrollmentException.class);

            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }

        @ParameterizedTest(name = "Existing: {0} {1}:00-{2}:00, New: {3} {4}:00-{5}:00 -> conflict={6}")
        @MethodSource("scheduleConflictScenarios")
        @DisplayName("Should detect schedule conflicts correctly")
        void testEnrollStudent_ScheduleConflict(
                DayOfWeek existingDay, int existingStart, int existingEnd,
                DayOfWeek newDay, int newStart, int newEnd,
                boolean shouldConflict) {

            // Given
            Schedule existingSchedule = TestDataBuilder.aScheduleOn(existingDay, existingStart, existingEnd)
                    .id(2L).build();
            Schedule newSchedule = TestDataBuilder.aScheduleOn(newDay, newStart, newEnd)
                    .id(1L).build();

            Section existingSection = TestDataBuilder.aSection()
                    .id(2L)
                    .sectionCode("CS102-A")
                    .course(course)
                    .room(room)
                    .term(term)
                    .schedule(existingSchedule)
                    .build();

            Section newSection = TestDataBuilder.aSection()
                    .id(1L)
                    .course(course)
                    .room(room)
                    .term(term)
                    .schedule(newSchedule)
                    .build();

            Enrollment existingEnrollment = TestDataBuilder.anEnrollment()
                    .id(1L)
                    .student(student)
                    .section(existingSection)
                    .status(EnrollmentStatus.ENROLLED)
                    .build();

            EnrollmentCreateRequest request = new EnrollmentCreateRequest(1L);

            when(studentService.getCurrentStudent()).thenReturn(student);
            when(sectionRepository.findById(1L)).thenReturn(Optional.of(newSection));
            when(enrollmentRepository.existsByStudentIdAndSectionId(anyLong(), anyLong())).thenReturn(false);
            when(enrollmentRepository.findActiveEnrollmentsForScheduleConflictCheck(anyLong(), anyLong()))
                    .thenReturn(List.of(existingEnrollment));

            // When & Then
            if (shouldConflict) {
                assertThatThrownBy(() -> enrollmentService.createEnrollment(request))
                        .isInstanceOf(ScheduleConflictException.class);
                verify(enrollmentRepository, never()).save(any(Enrollment.class));
            } else {
                Enrollment savedEnrollment = TestDataBuilder.anEnrollment()
                        .id(2L).student(student).section(newSection).build();
                when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(savedEnrollment);
                when(sectionRepository.save(any(Section.class))).thenReturn(newSection);
                when(studentService.toDto(any(Student.class))).thenReturn(null);

                EnrollmentDto result = enrollmentService.createEnrollment(request);
                assertThat(result).isNotNull();
            }
        }

        static Stream<Arguments> scheduleConflictScenarios() {
            return Stream.of(
                    // Same day, overlapping times -> conflict
                    Arguments.of(DayOfWeek.MONDAY, 8, 10, DayOfWeek.MONDAY, 9, 11, true),
                    Arguments.of(DayOfWeek.MONDAY, 9, 11, DayOfWeek.MONDAY, 8, 10, true),
                    Arguments.of(DayOfWeek.MONDAY, 8, 12, DayOfWeek.MONDAY, 9, 11, true),
                    Arguments.of(DayOfWeek.MONDAY, 9, 10, DayOfWeek.MONDAY, 8, 11, true),

                    // Same day, non-overlapping times -> no conflict
                    Arguments.of(DayOfWeek.MONDAY, 8, 10, DayOfWeek.MONDAY, 10, 12, false),
                    Arguments.of(DayOfWeek.MONDAY, 10, 12, DayOfWeek.MONDAY, 8, 10, false),
                    Arguments.of(DayOfWeek.MONDAY, 8, 9, DayOfWeek.MONDAY, 14, 16, false),

                    // Different days -> no conflict
                    Arguments.of(DayOfWeek.MONDAY, 8, 10, DayOfWeek.TUESDAY, 8, 10, false),
                    Arguments.of(DayOfWeek.WEDNESDAY, 9, 11, DayOfWeek.FRIDAY, 9, 11, false)
            );
        }
    }

    @Nested
    @DisplayName("Drop Enrollment Tests")
    class DropEnrollmentTests {

        @Test
        @DisplayName("Should drop enrollment successfully")
        void testDropEnrollment_Success() {
            // Given
            Enrollment enrollment = TestDataBuilder.anEnrollment()
                    .id(1L)
                    .student(student)
                    .section(section)
                    .status(EnrollmentStatus.ENROLLED)
                    .build();

            when(studentService.getCurrentStudent()).thenReturn(student);
            when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);
            when(sectionRepository.save(any(Section.class))).thenReturn(section);

            // When
            enrollmentService.dropEnrollment(1L);

            // Then
            verify(enrollmentRepository).save(argThat(e -> e.getStatus() == EnrollmentStatus.DROPPED));
            verify(sectionRepository).save(any(Section.class));
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when dropping other's enrollment")
        void testDropEnrollment_NotOwner_ThrowsException() {
            // Given
            User otherUser = TestDataBuilder.aUser().id(2L).username("other").build();
            Student otherStudent = TestDataBuilder.aStudent().id(2L).user(otherUser).build();

            Enrollment enrollment = TestDataBuilder.anEnrollment()
                    .id(1L)
                    .student(otherStudent)
                    .section(section)
                    .status(EnrollmentStatus.ENROLLED)
                    .build();

            when(studentService.getCurrentStudent()).thenReturn(student);
            when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

            // When & Then
            assertThatThrownBy(() -> enrollmentService.dropEnrollment(1L))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("your own");

            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should throw InvalidEnrollmentStatusException when dropping already dropped enrollment")
        void testDropEnrollment_AlreadyDropped_ThrowsException() {
            // Given
            Enrollment enrollment = TestDataBuilder.anEnrollment()
                    .id(1L)
                    .student(student)
                    .section(section)
                    .status(EnrollmentStatus.DROPPED)
                    .build();

            when(studentService.getCurrentStudent()).thenReturn(student);
            when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

            // When & Then
            assertThatThrownBy(() -> enrollmentService.dropEnrollment(1L))
                    .isInstanceOf(InvalidEnrollmentStatusException.class);

            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }
    }
}
