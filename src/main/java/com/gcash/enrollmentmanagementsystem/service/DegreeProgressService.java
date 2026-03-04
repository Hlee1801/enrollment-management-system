package com.gcash.enrollmentmanagementsystem.service;

import com.gcash.enrollmentmanagementsystem.dto.*;
import com.gcash.enrollmentmanagementsystem.entity.*;
import com.gcash.enrollmentmanagementsystem.enums.EnrollmentStatus;
import com.gcash.enrollmentmanagementsystem.exception.ResourceNotFoundException;
import com.gcash.enrollmentmanagementsystem.repository.CourseRepository;
import com.gcash.enrollmentmanagementsystem.repository.DegreeRepository;
import com.gcash.enrollmentmanagementsystem.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DegreeProgressService {

    private final DegreeRepository degreeRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentService studentService;

    @Transactional(readOnly = true)
    public DegreeProgressDto getDegreeProgress(Long studentId) {
        Student student = studentService.getStudentEntityById(studentId);
        return calculateDegreeProgress(student);
    }

    @Transactional(readOnly = true)
    public DegreeProgressDto getCurrentStudentDegreeProgress() {
        Student student = studentService.getCurrentStudent();
        return calculateDegreeProgress(student);
    }

    @Transactional(readOnly = true)
    public List<DegreeDto> getAllDegrees() {
        return degreeRepository.findAll().stream()
                .map(this::toDegreeDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DegreeDto getDegreeById(Long id) {
        Degree degree = degreeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Degree", "id", id));
        return toDegreeDto(degree);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByDegree(Long degreeId) {
        if (!degreeRepository.existsById(degreeId)) {
            throw new ResourceNotFoundException("Degree", "id", degreeId);
        }
        return courseRepository.findByDegreeId(degreeId).stream()
                .map(this::toCourseDto)
                .toList();
    }

    private DegreeProgressDto calculateDegreeProgress(Student student) {
        Degree degree = student.getDegree();
        if (degree == null) {
            throw new ResourceNotFoundException("Student", "degree", student.getId());
        }

        List<Course> requiredCourses = courseRepository.findByDegreeId(degree.getId());
        List<Enrollment> studentEnrollments = enrollmentRepository.findByStudentIdAndDegreeId(
                student.getId(), degree.getId());

        Map<Long, Enrollment> courseEnrollmentMap = studentEnrollments.stream()
                .collect(Collectors.toMap(
                        e -> e.getSection().getCourse().getId(),
                        e -> e,
                        (e1, e2) -> e1.getStatus() == EnrollmentStatus.COMPLETED ? e1 : e2
                ));

        List<CourseProgressDto> courseProgressList = new ArrayList<>();
        int completedCourses = 0;
        int enrolledCourses = 0;
        int completedUnits = 0;
        int enrolledUnits = 0;
        int totalUnits = 0;

        for (Course course : requiredCourses) {
            totalUnits += course.getUnits();
            Enrollment enrollment = courseEnrollmentMap.get(course.getId());

            CourseProgressDto.CourseProgressDtoBuilder progressBuilder = CourseProgressDto.builder()
                    .courseId(course.getId())
                    .courseCode(course.getCourseCode())
                    .courseName(course.getCourseName())
                    .units(course.getUnits());

            if (enrollment != null) {
                progressBuilder.status(enrollment.getStatus())
                        .enrollmentId(enrollment.getId())
                        .sectionId(enrollment.getSection().getId())
                        .sectionCode(enrollment.getSection().getSectionCode());

                if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
                    completedCourses++;
                    completedUnits += course.getUnits();
                } else if (enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
                    enrolledCourses++;
                    enrolledUnits += course.getUnits();
                }
            }

            courseProgressList.add(progressBuilder.build());
        }

        int totalCourses = requiredCourses.size();
        int remainingCourses = totalCourses - completedCourses - enrolledCourses;
        int remainingUnits = totalUnits - completedUnits - enrolledUnits;
        double completionPercentage = totalCourses > 0
                ? (double) completedCourses / totalCourses * 100
                : 0.0;
        boolean isCompleted = completedCourses == totalCourses && totalCourses > 0;

        return DegreeProgressDto.builder()
                .degree(toDegreeDto(degree))
                .student(studentService.toDto(student))
                .totalCourses(totalCourses)
                .completedCourses(completedCourses)
                .enrolledCourses(enrolledCourses)
                .remainingCourses(remainingCourses)
                .totalUnits(totalUnits)
                .completedUnits(completedUnits)
                .enrolledUnits(enrolledUnits)
                .remainingUnits(remainingUnits)
                .completionPercentage(Math.round(completionPercentage * 100.0) / 100.0)
                .isCompleted(isCompleted)
                .courseProgress(courseProgressList)
                .build();
    }

    private DegreeDto toDegreeDto(Degree degree) {
        if (degree == null) return null;
        List<Course> courses = courseRepository.findByDegreeId(degree.getId());
        int totalUnits = courses.stream().mapToInt(Course::getUnits).sum();

        return DegreeDto.builder()
                .id(degree.getId())
                .name(degree.getName())
                .description(degree.getDescription())
                .totalCourses(courses.size())
                .totalUnits(totalUnits)
                .build();
    }

    private CourseDto toCourseDto(Course course) {
        if (course == null) return null;
        return CourseDto.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .units(course.getUnits())
                .degreeName(course.getDegree() != null ? course.getDegree().getName() : null)
                .build();
    }
}
