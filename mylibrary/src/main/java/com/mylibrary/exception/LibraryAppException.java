package com.mylibrary.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class LibraryAppException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected LibraryAppException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus  = httpStatus;
        this.errorCode   = errorCode;
    }
}
