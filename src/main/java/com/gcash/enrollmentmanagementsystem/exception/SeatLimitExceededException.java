package com.gcash.enrollmentmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SeatLimitExceededException extends RuntimeException {

    public SeatLimitExceededException(String message) {
        super(message);
    }

    public SeatLimitExceededException(String sectionCode, int maxSeats) {
        super(String.format("Section '%s' has reached its maximum capacity of %d seats", sectionCode, maxSeats));
    }
}
