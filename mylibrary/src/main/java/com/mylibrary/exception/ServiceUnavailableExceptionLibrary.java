package com.mylibrary.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableExceptionLibrary extends LibraryAppException {
    public ServiceUnavailableExceptionLibrary(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, ErrorCodes.SERVICE_UNAVAILABLE, message);
    }
}