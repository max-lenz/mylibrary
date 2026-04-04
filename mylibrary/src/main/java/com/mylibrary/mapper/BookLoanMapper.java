package com.mylibrary.mapper;

import com.mylibrary.dto.bookloan.BookLoanDTO;
import com.mylibrary.entity.BookLoan;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BookLoanMapper {
    public static BookLoanDTO toBookLoanDTO(BookLoan entity) {
        boolean isOverdue = entity.getReturnDate() == null
                && LocalDate.now().isAfter(entity.getDueDate());
        return new BookLoanDTO(
                entity.getId(),
                entity.getBook().getId(),
                entity.getReader().getId(),
                entity.getLoanDate(),
                entity.getDueDate(),
                entity.getReturnDate(),
                entity.getLoanStatus(),
                isOverdue
        );
    }
}

