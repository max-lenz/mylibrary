package com.mylibrary.controller.book;

import com.mylibrary.dto.book.BookDTO;
import com.mylibrary.dto.book.UpdateBookPayload;
import com.mylibrary.service.BookService;
import com.mylibrary.validation.ValidId;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("api/books/{id}")
public class BookController {
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public BookDTO getBook(@ValidId @PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@ValidId @PathVariable Long id) {
        bookService.deleteBookById(id);
    }

    @PutMapping
    public BookDTO updateBook(@ValidId @PathVariable Long id,
                              @Valid @RequestBody UpdateBookPayload newBook) {
     return bookService.updateBookById(id, newBook);
    }
}
