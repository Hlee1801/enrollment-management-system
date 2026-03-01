package com.gcash.enrollmentmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermDto {

    private Long id;
    private String termName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}
