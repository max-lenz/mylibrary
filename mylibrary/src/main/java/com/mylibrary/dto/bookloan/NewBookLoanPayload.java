package com.mylibrary.dto.bookloan;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record NewBookLoanPayload(
        @NotNull(message = "BookID is required")
        Long bookId,
        @NotNull(message = "ReaderID is required")
        Long readerId,
        @NotNull(message = "Due date is required YYYY-MM-DD")
        @Future(message = "Due date must be in the future")
        LocalDate dueDate) {
}
