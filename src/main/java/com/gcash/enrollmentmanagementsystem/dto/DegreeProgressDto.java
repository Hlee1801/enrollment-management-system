package com.gcash.enrollmentmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DegreeProgressDto {

    private DegreeDto degree;
    private StudentDto student;
    private Integer totalCourses;
    private Integer completedCourses;
    private Integer enrolledCourses;
    private Integer remainingCourses;
    private Integer totalUnits;
    private Integer completedUnits;
    private Integer enrolledUnits;
    private Integer remainingUnits;
    private Double completionPercentage;
    private Boolean isCompleted;
    private List<CourseProgressDto> courseProgress;
}
