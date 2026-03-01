package com.gcash.enrollmentmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ScheduleConflictException extends RuntimeException {

    public ScheduleConflictException(String message) {
        super(message);
    }

    public ScheduleConflictException(String newSectionCode, String existingSectionCode) {
        super(String.format("Schedule conflict: Section '%s' overlaps with your enrolled section '%s'",
                newSectionCode, existingSectionCode));
    }
}
