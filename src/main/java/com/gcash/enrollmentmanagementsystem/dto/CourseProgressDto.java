package com.gcash.enrollmentmanagementsystem.dto;

import com.gcash.enrollmentmanagementsystem.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressDto {

    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer units;
    private EnrollmentStatus status;
    private Long enrollmentId;
    private Long sectionId;
    private String sectionCode;
}
