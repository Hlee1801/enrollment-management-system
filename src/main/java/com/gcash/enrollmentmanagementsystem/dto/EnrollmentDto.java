package com.gcash.enrollmentmanagementsystem.dto;

import com.gcash.enrollmentmanagementsystem.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDto {

    private Long id;
    private StudentDto student;
    private SectionDto section;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
}
