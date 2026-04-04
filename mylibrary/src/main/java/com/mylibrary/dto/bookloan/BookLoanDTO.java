package com.mylibrary.dto.bookloan;

import com.mylibrary.entity.BookLoanStatus;

import java.time.LocalDate;

public record BookLoanDTO(Long loanId,
                          Long bookId,
                          Long readerId,
                          LocalDate loanDate,
                          LocalDate dueDate,
                          LocalDate returnDate,
                          BookLoanStatus loanStatus,
                          boolean overdue) {
}
