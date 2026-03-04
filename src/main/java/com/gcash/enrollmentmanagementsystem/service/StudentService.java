package com.gcash.enrollmentmanagementsystem.service;

import com.gcash.enrollmentmanagementsystem.dto.EnrollmentDto;
import com.gcash.enrollmentmanagementsystem.dto.StudentDto;
import com.gcash.enrollmentmanagementsystem.dto.StudentUpdateRequest;
import com.gcash.enrollmentmanagementsystem.entity.Enrollment;
import com.gcash.enrollmentmanagementsystem.entity.Student;
import com.gcash.enrollmentmanagementsystem.exception.ResourceNotFoundException;
import com.gcash.enrollmentmanagementsystem.repository.EnrollmentRepository;
import com.gcash.enrollmentmanagementsystem.repository.StudentRepository;
import com.gcash.enrollmentmanagementsystem.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public Page<StudentDto> getAllStudents(Pageable pageable) {
        Page<Student> students = studentRepository.findAll(pageable);
        List<StudentDto> dtoList = students.getContent().stream()
                .map(this::toDto)
                .toList();
        return new PageImpl<>(dtoList, pageable, students.getTotalElements());
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return toDto(student);
    }

    @Transactional(readOnly = true)
    public StudentDto getCurrentStudentProfile() {
        Student student = getCurrentStudent();
        return toDto(student);
    }

    @Transactional
    public StudentDto updateCurrentStudentProfile(StudentUpdateRequest request) {
        Student student = getCurrentStudent();

        if (request.getFirstName() != null) {
            student.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            student.setLastName(request.getLastName());
        }
        if (request.getDateOfBirth() != null) {
            student.setDateOfBirth(request.getDateOfBirth());
        }

        student = studentRepository.save(student);
        log.info("Updated student profile for: {}", student.getStudentNumber());

        return toDto(student);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getCurrentStudentEnrollments() {
        Student student = getCurrentStudent();
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        return enrollments.stream()
                .map(this::toEnrollmentDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getStudentEnrollmentsByTermId(Long termId) {
        Student student = getCurrentStudent();
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndTermId(student.getId(), termId);
        return enrollments.stream()
                .map(this::toEnrollmentDto)
                .toList();
    }

    public Student getCurrentStudent() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return studentRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", userPrincipal.getId()));
    }

    @Transactional(readOnly = true)
    public Student getStudentEntityById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }

    public StudentDto toDto(Student student) {
        if (student == null) return null;
        return StudentDto.builder()
                .id(student.getId())
                .studentNumber(student.getStudentNumber())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .dateOfBirth(student.getDateOfBirth())
                .email(student.getUser() != null ? student.getUser().getEmail() : null)
                .username(student.getUser() != null ? student.getUser().getUsername() : null)
                .degreeId(student.getDegree() != null ? student.getDegree().getId() : null)
                .degreeName(student.getDegree() != null ? student.getDegree().getName() : null)
                .build();
    }

    private EnrollmentDto toEnrollmentDto(Enrollment enrollment) {
        if (enrollment == null) return null;
        return EnrollmentDto.builder()
                .id(enrollment.getId())
                .student(toDto(enrollment.getStudent()))
                .section(SectionService.toSectionDto(enrollment.getSection()))
                .status(enrollment.getStatus())
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }
}
