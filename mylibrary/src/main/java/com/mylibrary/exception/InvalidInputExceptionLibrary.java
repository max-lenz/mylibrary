package com.mylibrary.exception;

import org.springframework.http.HttpStatus;

public class InvalidInputExceptionLibrary extends LibraryAppException {
    public InvalidInputExceptionLibrary(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT, message);
    }
}
