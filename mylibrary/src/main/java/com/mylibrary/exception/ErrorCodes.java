package com.mylibrary.exception;

public final class ErrorCodes {

    private ErrorCodes() {}

    public static final String INVALID_INPUT = "400.100";
    public static final String VALIDATION_ERROR = "400.101";
    public static final String RESOURCE_NOT_FOUND = "404.100";
    public static final String RESOURCE_CONFLICT = "409.100";
    public static final String OPTIMISTIC_LOCK = "409.101";
    public static final String SERVICE_UNAVAILABLE = "503.100";
}