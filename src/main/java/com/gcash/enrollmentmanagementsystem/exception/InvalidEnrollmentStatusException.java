package com.gcash.enrollmentmanagementsystem.exception;

import com.gcash.enrollmentmanagementsystem.enums.EnrollmentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEnrollmentStatusException extends RuntimeException {

    public InvalidEnrollmentStatusException(String message) {
        super(message);
    }

    public InvalidEnrollmentStatusException(EnrollmentStatus currentStatus, String action) {
        super(String.format("Cannot %s enrollment with status '%s'", action, currentStatus));
    }
}
