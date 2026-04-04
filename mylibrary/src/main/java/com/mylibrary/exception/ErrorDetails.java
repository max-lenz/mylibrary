package com.mylibrary.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

public final class ErrorDetails {

    private final int statusCode;
    private final String errorCode;
    private final String message;
    private final LocalDateTime timestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, String> errors;

    public ErrorDetails(int statusCode, String errorCode, String message) {
        this(statusCode, errorCode, message, null);
    }

    public ErrorDetails(int statusCode, String errorCode, String message,
                        Map<String, String> errors) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }

    public int getStatusCode() { return statusCode; }
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, String> getErrors() { return errors; }
}