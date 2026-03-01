package com.gcash.enrollmentmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEnrollmentException extends RuntimeException {

    public DuplicateEnrollmentException(String message) {
        super(message);
    }

    public DuplicateEnrollmentException(String studentNumber, String sectionCode) {
        super(String.format("Student '%s' is already enrolled in section '%s'", studentNumber, sectionCode));
    }
}
