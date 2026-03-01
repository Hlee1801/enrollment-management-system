package com.gcash.enrollmentmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionDto {

    private Long id;
    private String sectionCode;
    private CourseDto course;
    private RoomDto room;
    private TermDto term;
    private ScheduleDto schedule;
    private Integer maxSeats;
    private Integer currentEnrollment;
    private Integer availableSeats;
}
