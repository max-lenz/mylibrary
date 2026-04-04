package com.mylibrary.controller.book;

import com.mylibrary.dto.book.BookDTO;
import com.mylibrary.dto.book.NewBookPayload;
import com.mylibrary.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/books")
public class BooksController {
    private final BookService bookService;

    @Autowired
    public BooksController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Page<BookDTO> getAllBooks(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(defaultValue = "ASC") Sort.Direction direction,
                                     @RequestParam(defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, direction, sortBy);
        return bookService.getBooks(pageable);
    }

    @GetMapping("search")
    public Page<BookDTO> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) Boolean availableOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return bookService.searchBooks(title, author, isbn, yearFrom, yearTo, availableOnly, pageable);
    }


    @PostMapping
    public ResponseEntity<BookDTO> addBook(@Valid @RequestBody NewBookPayload newBook) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addNewBook(newBook));
    }
}
