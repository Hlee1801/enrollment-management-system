package com.gcash.enrollmentmanagementsystem.controller;

import com.gcash.enrollmentmanagementsystem.dto.*;
import com.gcash.enrollmentmanagementsystem.service.EnrollmentService;
import com.gcash.enrollmentmanagementsystem.service.SectionService;
import com.gcash.enrollmentmanagementsystem.service.StudentService;
import com.gcash.enrollmentmanagementsystem.service.TermService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management APIs")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final StudentService studentService;
    private final SectionService sectionService;
    private final TermService termService;
    private final EnrollmentService enrollmentService;

    @Operation(summary = "Get all students", description = "Retrieve a paginated list of all students")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/students")
    public ResponseEntity<Page<StudentDto>> getAllStudents(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "studentNumber") Pageable pageable
    ) {
        Page<StudentDto> students = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(students);
    }

    @Operation(summary = "Get students in section", description = "Retrieve students enrolled in a specific section")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Section not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/sections/{id}/students")
    public ResponseEntity<Page<StudentDto>> getStudentsInSection(
            @Parameter(description = "Section ID") @PathVariable Long id,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "studentNumber") Pageable pageable
    ) {
        Page<StudentDto> students = sectionService.getStudentsInSection(id, pageable);
        return ResponseEntity.ok(students);
    }

    @Operation(summary = "Create section", description = "Create a new section for a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Section created successfully",
                    content = @Content(schema = @Schema(implementation = SectionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Course, Room, Term, or Schedule not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PostMapping("/sections")
    public ResponseEntity<SectionDto> createSection(
            @Valid @RequestBody SectionCreateRequest request
    ) {
        SectionDto section = sectionService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(section);
    }

    @Operation(summary = "Update section", description = "Update an existing section")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Section updated successfully",
                    content = @Content(schema = @Schema(implementation = SectionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Section not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PutMapping("/sections/{id}")
    public ResponseEntity<SectionDto> updateSection(
            @Parameter(description = "Section ID") @PathVariable Long id,
            @Valid @RequestBody SectionUpdateRequest request
    ) {
        SectionDto section = sectionService.updateSection(id, request);
        return ResponseEntity.ok(section);
    }

    @Operation(summary = "Delete section", description = "Delete an existing section")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Section deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete section with active enrollments"),
            @ApiResponse(responseCode = "404", description = "Section not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Void> deleteSection(
            @Parameter(description = "Section ID") @PathVariable Long id
    ) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all terms", description = "Retrieve all academic terms")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Terms retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/terms")
    public ResponseEntity<List<TermDto>> getAllTerms() {
        List<TermDto> terms = termService.getAllTerms();
        return ResponseEntity.ok(terms);
    }

    @Operation(summary = "Create term", description = "Create a new academic term")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Term created successfully",
                    content = @Content(schema = @Schema(implementation = TermDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or term already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PostMapping("/terms")
    public ResponseEntity<TermDto> createTerm(
            @Valid @RequestBody TermCreateRequest request
    ) {
        TermDto term = termService.createTerm(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(term);
    }

    @Operation(summary = "Get all sections", description = "Retrieve all sections for the active term")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sections retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/sections")
    public ResponseEntity<List<SectionDto>> getAllSections() {
        List<SectionDto> sections = sectionService.getAllSectionsForAdmin();
        return ResponseEntity.ok(sections);
    }

    @Operation(summary = "Get enrollments in section",
            description = "Retrieve all active enrollments in a specific section")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Section not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/sections/{id}/enrollments")
    public ResponseEntity<List<EnrollmentDto>> getEnrollmentsInSection(
            @Parameter(description = "Section ID") @PathVariable Long id
    ) {
        List<EnrollmentDto> enrollments = enrollmentService.getEnrollmentsBySection(id);
        return ResponseEntity.ok(enrollments);
    }

    @Operation(summary = "Mark enrollment as completed",
            description = "Mark a student's enrollment as completed (course passed)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollment marked as completed",
                    content = @Content(schema = @Schema(implementation = EnrollmentDto.class))),
            @ApiResponse(responseCode = "400", description = "Cannot complete dropped enrollment"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PutMapping("/enrollments/{id}/complete")
    public ResponseEntity<EnrollmentDto> markEnrollmentCompleted(
            @Parameter(description = "Enrollment ID") @PathVariable Long id
    ) {
        EnrollmentDto enrollment = enrollmentService.markAsCompleted(id);
        return ResponseEntity.ok(enrollment);
    }
}
