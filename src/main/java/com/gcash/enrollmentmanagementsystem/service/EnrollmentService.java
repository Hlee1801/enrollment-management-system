package com.gcash.enrollmentmanagementsystem.service;

import com.gcash.enrollmentmanagementsystem.dto.EnrollmentCreateRequest;
import com.gcash.enrollmentmanagementsystem.dto.EnrollmentDto;
import com.gcash.enrollmentmanagementsystem.dto.EnrollmentUpdateRequest;
import com.gcash.enrollmentmanagementsystem.entity.Enrollment;
import com.gcash.enrollmentmanagementsystem.entity.Schedule;
import com.gcash.enrollmentmanagementsystem.entity.Section;
import com.gcash.enrollmentmanagementsystem.entity.Student;
import com.gcash.enrollmentmanagementsystem.enums.EnrollmentStatus;
import com.gcash.enrollmentmanagementsystem.exception.*;
import com.gcash.enrollmentmanagementsystem.repository.EnrollmentRepository;
import com.gcash.enrollmentmanagementsystem.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final StudentService studentService;

    @Transactional
    public EnrollmentDto createEnrollment(EnrollmentCreateRequest request) {
        Student student = studentService.getCurrentStudent();
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));

        checkCourseInDegree(student, section);

        if (enrollmentRepository.existsByStudentIdAndSectionId(student.getId(), section.getId())) {
            throw new DuplicateEnrollmentException(student.getStudentNumber(), section.getSectionCode());
        }

        if (section.getCurrentEnrollment() >= section.getMaxSeats()) {
            throw new SeatLimitExceededException(section.getSectionCode(), section.getMaxSeats());
        }

        checkScheduleConflict(student, section);

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .section(section)
                .status(EnrollmentStatus.ENROLLED)
                .build();

        enrollment = enrollmentRepository.save(enrollment);

        section.setCurrentEnrollment(section.getCurrentEnrollment() + 1);
        sectionRepository.save(section);

        log.info("Student {} enrolled in section {} with status ENROLLED",
                student.getStudentNumber(), section.getSectionCode());

        return toDto(enrollment);
    }

    @Transactional
    public EnrollmentDto updateEnrollment(Long enrollmentId, EnrollmentUpdateRequest request) {
        Student student = studentService.getCurrentStudent();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        if (!enrollment.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedException("You can only modify your own enrollments");
        }

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new InvalidEnrollmentStatusException(enrollment.getStatus(), "update");
        }

        Section oldSection = enrollment.getSection();
        Section newSection = sectionRepository.findById(request.getNewSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getNewSectionId()));

        if (oldSection.getId().equals(newSection.getId())) {
            return toDto(enrollment);
        }

        if (!oldSection.getCourse().getId().equals(newSection.getCourse().getId())) {
            throw new BadRequestException("Cannot change to a section of a different course");
        }

        if (newSection.getCurrentEnrollment() >= newSection.getMaxSeats()) {
            throw new SeatLimitExceededException(newSection.getSectionCode(), newSection.getMaxSeats());
        }

        checkScheduleConflictExcluding(student, newSection, enrollment);

        enrollment.setSection(newSection);
        enrollment = enrollmentRepository.save(enrollment);

        oldSection.setCurrentEnrollment(oldSection.getCurrentEnrollment() - 1);
        newSection.setCurrentEnrollment(newSection.getCurrentEnrollment() + 1);
        sectionRepository.save(oldSection);
        sectionRepository.save(newSection);

        log.info("Student {} changed from section {} to section {}",
                student.getStudentNumber(), oldSection.getSectionCode(), newSection.getSectionCode());

        return toDto(enrollment);
    }

    @Transactional
    public void dropEnrollment(Long enrollmentId) {
        Student student = studentService.getCurrentStudent();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        if (!enrollment.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedException("You can only drop your own enrollments");
        }

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new InvalidEnrollmentStatusException(enrollment.getStatus(), "drop");
        }

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);

        Section section = enrollment.getSection();
        section.setCurrentEnrollment(section.getCurrentEnrollment() - 1);
        sectionRepository.save(section);

        log.info("Student {} dropped enrollment from section {}",
                student.getStudentNumber(), section.getSectionCode());
    }

    @Transactional(readOnly = true)
    public EnrollmentDto getEnrollmentById(Long id) {
        Student student = studentService.getCurrentStudent();
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));

        if (!enrollment.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedException("You can only view your own enrollments");
        }

        return toDto(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getStudentEnrollments(Long termId) {
        Student student = studentService.getCurrentStudent();
        List<Enrollment> enrollments;

        if (termId != null) {
            enrollments = enrollmentRepository.findByStudentIdAndTermId(student.getId(), termId);
        } else {
            enrollments = enrollmentRepository.findByStudentId(student.getId());
        }

        return enrollments.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getEnrollmentsBySection(Long sectionId) {
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

        return enrollmentRepository.findBySectionId(sectionId)
                .stream()
                .filter(e -> e.getStatus() != EnrollmentStatus.DROPPED) // Exclude dropped
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public EnrollmentDto markAsCompleted(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new InvalidEnrollmentStatusException(enrollment.getStatus(), "mark as completed");
        }

        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            return toDto(enrollment); // Already completed
        }

        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment = enrollmentRepository.save(enrollment);

        log.info("Enrollment {} for student {} in section {} marked as COMPLETED",
                enrollmentId,
                enrollment.getStudent().getStudentNumber(),
                enrollment.getSection().getSectionCode());

        return toDto(enrollment);
    }

    private void checkScheduleConflict(Student student, Section newSection) {
        Long termId = newSection.getTerm().getId();
        Schedule newSchedule = newSection.getSchedule();

        List<Enrollment> existingEnrollments = enrollmentRepository
                .findActiveEnrollmentsForScheduleConflictCheck(student.getId(), termId);

        for (Enrollment enrollment : existingEnrollments) {
            Schedule existingSchedule = enrollment.getSection().getSchedule();

            if (hasTimeOverlap(existingSchedule, newSchedule)) {
                throw new ScheduleConflictException(
                        newSection.getSectionCode(),
                        enrollment.getSection().getSectionCode());
            }
        }
    }

    private void checkScheduleConflictExcluding(Student student, Section newSection, Enrollment excludeEnrollment) {
        Long termId = newSection.getTerm().getId();
        Schedule newSchedule = newSection.getSchedule();

        List<Enrollment> existingEnrollments = enrollmentRepository
                .findActiveEnrollmentsForScheduleConflictCheck(student.getId(), termId);

        for (Enrollment enrollment : existingEnrollments) {
            if (enrollment.getId().equals(excludeEnrollment.getId())) {
                continue;
            }

            Schedule existingSchedule = enrollment.getSection().getSchedule();

            if (hasTimeOverlap(existingSchedule, newSchedule)) {
                throw new ScheduleConflictException(
                        newSection.getSectionCode(),
                        enrollment.getSection().getSectionCode());
            }
        }
    }

    private boolean hasTimeOverlap(Schedule schedule1, Schedule schedule2) {
        if (schedule1.getDayOfWeek() != schedule2.getDayOfWeek()) {
            return false;
        }
        return schedule1.getStartTime().isBefore(schedule2.getEndTime()) &&
                schedule2.getStartTime().isBefore(schedule1.getEndTime());
    }

    private void checkCourseInDegree(Student student, Section section) {
        if (student.getDegree() == null) {
            throw new BadRequestException("Student does not have a degree assigned");
        }

        Long courseDegreeId = section.getCourse().getDegree().getId();
        Long studentDegreeId = student.getDegree().getId();

        if (!courseDegreeId.equals(studentDegreeId)) {
            throw new CourseNotInDegreeException(
                    section.getCourse().getCourseCode(),
                    student.getDegree().getName());
        }
    }

    private EnrollmentDto toDto(Enrollment enrollment) {
        if (enrollment == null) return null;
        return EnrollmentDto.builder()
                .id(enrollment.getId())
                .student(studentService.toDto(enrollment.getStudent()))
                .section(SectionService.toSectionDto(enrollment.getSection()))
                .status(enrollment.getStatus())
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }
}
