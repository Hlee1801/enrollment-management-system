package com.gcash.enrollmentmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DegreeDto {

    private Long id;
    private String name;
    private String description;
    private Integer totalCourses;
    private Integer totalUnits;
}
