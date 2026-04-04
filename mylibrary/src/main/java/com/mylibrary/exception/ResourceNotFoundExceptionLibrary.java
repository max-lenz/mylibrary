package com.mylibrary.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundExceptionLibrary extends LibraryAppException {
    public ResourceNotFoundExceptionLibrary(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCodes.RESOURCE_NOT_FOUND, message);
    }
}
