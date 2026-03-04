package com.gcash.enrollmentmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CourseNotInDegreeException extends RuntimeException {

    public CourseNotInDegreeException(String message) {
        super(message);
    }

    public CourseNotInDegreeException(String courseCode, String degreeName) {
        super(String.format("Course '%s' is not part of the required courses for degree '%s'", courseCode, degreeName));
    }
}
