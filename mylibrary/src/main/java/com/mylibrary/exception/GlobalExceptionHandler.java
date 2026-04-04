package com.mylibrary.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LibraryAppException.class)
    public ResponseEntity<ErrorDetails> handleAppException(LibraryAppException ex) {
        log.warn("App exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(new ErrorDetails(
                        ex.getHttpStatus().value(),
                        ex.getErrorCode(),
                        ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}",
                ex.getBindingResult().getFieldErrors().stream()
                        .map(FieldError::getField)
                        .collect(Collectors.joining(", ")));
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDetails(
                        HttpStatus.BAD_REQUEST.value(),
                        ErrorCodes.VALIDATION_ERROR,
                        "Request validation failed",
                        errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDetails> handleConstraintViolation(
            ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> {
                            String path = v.getPropertyPath().toString();
                            return path.contains(".")
                                    ? path.substring(path.lastIndexOf('.') + 1)
                                    : path;
                        },
                        ConstraintViolation::getMessage,
                        (a, b) -> a,
                        LinkedHashMap::new));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDetails(
                        HttpStatus.BAD_REQUEST.value(),
                        ErrorCodes.VALIDATION_ERROR,
                        "Constraint validation failed",
                        errors));
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorDetails> handleOptimisticLock(OptimisticLockException ex) {
        log.warn("Optimistic lock conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorDetails(
                        HttpStatus.CONFLICT.value(),
                        ErrorCodes.OPTIMISTIC_LOCK,
                        "Data was modified by another user. Please refresh and try again."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDetails(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "500.000",
                        "An unexpected error occurred"));
    }
}