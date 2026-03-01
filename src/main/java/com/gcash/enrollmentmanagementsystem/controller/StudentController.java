package com.gcash.enrollmentmanagementsystem.controller;

import com.gcash.enrollmentmanagementsystem.dto.EnrollmentDto;
import com.gcash.enrollmentmanagementsystem.dto.StudentDto;
import com.gcash.enrollmentmanagementsystem.dto.StudentUpdateRequest;
import com.gcash.enrollmentmanagementsystem.service.StudentService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student", description = "Student profile management APIs")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "Get own profile", description = "Retrieve the current student's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Student profile not found")
    })
    @GetMapping("/me")
    public ResponseEntity<StudentDto> getOwnProfile() {
        StudentDto student = studentService.getCurrentStudentProfile();
        return ResponseEntity.ok(student);
    }

    @Operation(summary = "Update own profile", description = "Update the current student's profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Student profile not found")
    })
    @PutMapping("/me")
    public ResponseEntity<StudentDto> updateOwnProfile(
            @Valid @RequestBody StudentUpdateRequest request
    ) {
        StudentDto student = studentService.updateCurrentStudentProfile(request);
        return ResponseEntity.ok(student);
    }

    @Operation(summary = "Get own enrollments", description = "Retrieve the current student's enrollments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Student profile not found")
    })
    @GetMapping("/me/enrollments")
    public ResponseEntity<List<EnrollmentDto>> getOwnEnrollments(
            @Parameter(description = "Filter by term ID (optional)")
            @RequestParam(required = false) Long termId
    ) {
        List<EnrollmentDto> enrollments;
        if (termId != null) {
            enrollments = studentService.getStudentEnrollmentsByTermId(termId);
        } else {
            enrollments = studentService.getCurrentStudentEnrollments();
        }
        return ResponseEntity.ok(enrollments);
    }
}
