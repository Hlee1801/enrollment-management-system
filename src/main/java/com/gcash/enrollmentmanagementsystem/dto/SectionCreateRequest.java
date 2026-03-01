package com.gcash.enrollmentmanagementsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionCreateRequest {

    @NotBlank(message = "Section code is required")
    @Size(max = 20, message = "Section code must not exceed 20 characters")
    private String sectionCode;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Term ID is required")
    private Long termId;

    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;

    @NotNull(message = "Max seats is required")
    @Min(value = 1, message = "Max seats must be at least 1")
    private Integer maxSeats;
}
