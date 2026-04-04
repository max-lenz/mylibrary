package com.mylibrary.controller.bookloan;

import com.mylibrary.dto.bookloan.BookLoanDTO;
import com.mylibrary.service.BookLoanService;
import com.mylibrary.validation.ValidId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("api/loans/{id}")
public class BookLoanController {
    private final BookLoanService bookLoanService;

    @Autowired
    public BookLoanController(BookLoanService bookLoanService) {
        this.bookLoanService = bookLoanService;
    }

    @PutMapping("return")
    public BookLoanDTO returnLoanedBook(@ValidId @PathVariable Long id) {
        return bookLoanService.returnBookByLoanId(id);
    }

    @PutMapping("renew")
    public BookLoanDTO renewLoan(@ValidId @PathVariable Long id,
                                 @RequestParam(required = false, defaultValue = "14") Integer days) {
        return bookLoanService.renewBookLoan(id, days);
    }

    @PutMapping("lost")
    public BookLoanDTO markAsLost(@ValidId @PathVariable Long id) {
        return bookLoanService.markAsLostByLoanId(id);
    }

}
