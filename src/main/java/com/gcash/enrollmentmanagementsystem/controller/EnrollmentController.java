package com.gcash.enrollmentmanagementsystem.controller;

import com.gcash.enrollmentmanagementsystem.dto.*;
import com.gcash.enrollmentmanagementsystem.service.EnrollmentService;
import com.gcash.enrollmentmanagementsystem.service.SectionService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollment", description = "Enrollment management APIs")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final SectionService sectionService;

    @Operation(summary = "Get available sections",
            description = "Retrieve available sections for enrollment, optionally filtered by term and/or course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sections retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Term or course not found")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<List<SectionDto>> getAvailableSections(
            @Parameter(description = "Filter by term ID (optional)")
            @RequestParam(required = false) Long termId,
            @Parameter(description = "Filter by course code (optional)")
            @RequestParam(required = false) String courseCode
    ) {
        List<SectionDto> sections = sectionService.getAvailableSections(termId, courseCode);
        return ResponseEntity.ok(sections);
    }

    @Operation(summary = "Enroll in section",
            description = "Enlist the current student in a section")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Enrolled successfully",
                    content = @Content(schema = @Schema(implementation = EnrollmentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Section not found"),
            @ApiResponse(responseCode = "409", description = "Seat limit exceeded, duplicate enrollment, or schedule conflict")
    })
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentDto> createEnrollment(
            @Valid @RequestBody EnrollmentCreateRequest request
    ) {
        EnrollmentDto enrollment = enrollmentService.createEnrollment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    @Operation(summary = "Update enrollment",
            description = "Change the section of an existing enrollment (same course only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollment updated successfully",
                    content = @Content(schema = @Schema(implementation = EnrollmentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or cannot change to different course"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Cannot modify other student's enrollment"),
            @ApiResponse(responseCode = "404", description = "Enrollment or section not found"),
            @ApiResponse(responseCode = "409", description = "Seat limit exceeded or schedule conflict")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentDto> updateEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable Long id,
            @Valid @RequestBody EnrollmentUpdateRequest request
    ) {
        EnrollmentDto enrollment = enrollmentService.updateEnrollment(id, request);
        return ResponseEntity.ok(enrollment);
    }

    @Operation(summary = "Drop enrollment",
            description = "Drop the student from an enrolled section")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Enrollment dropped successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot drop already dropped enrollment"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Cannot drop other student's enrollment"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> dropEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable Long id
    ) {
        enrollmentService.dropEnrollment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get enrollment by ID",
            description = "Retrieve a specific enrollment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollment retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EnrollmentDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Cannot view other student's enrollment"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentDto> getEnrollmentById(
            @Parameter(description = "Enrollment ID") @PathVariable Long id
    ) {
        EnrollmentDto enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(enrollment);
    }
}
