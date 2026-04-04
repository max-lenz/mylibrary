package com.mylibrary.controller.bookloan;

import com.mylibrary.dto.bookloan.BookLoanDTO;
import com.mylibrary.dto.bookloan.NewBookLoanPayload;
import com.mylibrary.service.BookLoanService;
import com.mylibrary.validation.ValidId;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("api/loans")
public class BookLoansController {
    private final BookLoanService bookLoanService;

    @Autowired
    public BookLoansController(BookLoanService bookLoanService) {
        this.bookLoanService = bookLoanService;
    }

    @PostMapping
    public ResponseEntity<BookLoanDTO> addLoan(@Valid @RequestBody NewBookLoanPayload newLoan) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookLoanService.addNewLoan(newLoan));
    }

    @GetMapping("reader/{readerId}")
    public Page<BookLoanDTO> getReaderHistory(@ValidId @PathVariable Long readerId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(defaultValue = "ASC") Sort.Direction direction,
                                              @RequestParam(defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, direction, sortBy);
        return bookLoanService.getFullReaderHistory(readerId, pageable);
    }

    @GetMapping("overdue")
    public Page<BookLoanDTO> getOverdueLoans(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "ASC") Sort.Direction direction,
                                             @RequestParam(defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, direction, sortBy);
        return bookLoanService.getAllOverdueLoans(pageable);
    }

    @GetMapping("lost")
    public Page<BookLoanDTO> getLostLoans(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(defaultValue = "ASC") Sort.Direction direction,
                                          @RequestParam(defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, direction, sortBy);
        return bookLoanService.getAllLostLoans(pageable);
    }
}
