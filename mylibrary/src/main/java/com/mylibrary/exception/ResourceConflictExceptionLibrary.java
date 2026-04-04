package com.mylibrary.exception;

import org.springframework.http.HttpStatus;

public class ResourceConflictExceptionLibrary extends LibraryAppException {
    public ResourceConflictExceptionLibrary(String message) {
        super(HttpStatus.CONFLICT, ErrorCodes.RESOURCE_CONFLICT, message);
    }
}
