package com.gcash.enrollmentmanagementsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionUpdateRequest {

    @Size(max = 20, message = "Section code must not exceed 20 characters")
    private String sectionCode;

    private Long roomId;

    private Long scheduleId;

    @Min(value = 1, message = "Max seats must be at least 1")
    private Integer maxSeats;
}
