package com.gcash.enrollmentmanagementsystem.controller;

import com.gcash.enrollmentmanagementsystem.dto.CourseDto;
import com.gcash.enrollmentmanagementsystem.dto.DegreeDto;
import com.gcash.enrollmentmanagementsystem.dto.DegreeProgressDto;
import com.gcash.enrollmentmanagementsystem.service.DegreeProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/degrees")
@RequiredArgsConstructor
@Tag(name = "Degree", description = "Degree and degree progress APIs")
@SecurityRequirement(name = "bearerAuth")
public class DegreeController {

    private final DegreeProgressService degreeProgressService;

    @Operation(summary = "Get all degrees", description = "Retrieve all available degrees")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Degrees retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<DegreeDto>> getAllDegrees() {
        List<DegreeDto> degrees = degreeProgressService.getAllDegrees();
        return ResponseEntity.ok(degrees);
    }

    @Operation(summary = "Get degree by ID", description = "Retrieve a specific degree by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Degree retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Degree not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DegreeDto> getDegreeById(
            @Parameter(description = "Degree ID") @PathVariable Long id) {
        DegreeDto degree = degreeProgressService.getDegreeById(id);
        return ResponseEntity.ok(degree);
    }

    @Operation(summary = "Get courses by degree", description = "Retrieve all courses required for a degree")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Degree not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}/courses")
    public ResponseEntity<List<CourseDto>> getCoursesByDegree(
            @Parameter(description = "Degree ID") @PathVariable Long id) {
        List<CourseDto> courses = degreeProgressService.getCoursesByDegree(id);
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "Get current student's degree progress",
            description = "Retrieve the degree completion progress for the currently authenticated student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Student or degree not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Student access required")
    })
    @GetMapping("/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<DegreeProgressDto> getCurrentStudentProgress() {
        DegreeProgressDto progress = degreeProgressService.getCurrentStudentDegreeProgress();
        return ResponseEntity.ok(progress);
    }

    @Operation(summary = "Get student's degree progress by ID",
            description = "Retrieve the degree completion progress for a specific student (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Student or degree not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/progress/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DegreeProgressDto> getStudentProgress(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        DegreeProgressDto progress = degreeProgressService.getDegreeProgress(studentId);
        return ResponseEntity.ok(progress);
    }
}
