package com.mylibrary.service;

import com.mylibrary.dto.book.BookDTO;
import com.mylibrary.dto.book.NewBookPayload;
import com.mylibrary.dto.book.UpdateBookPayload;
import com.mylibrary.entity.Book;
import com.mylibrary.entity.BookLoanStatus;
import com.mylibrary.exception.ResourceConflictExceptionLibrary;
import com.mylibrary.exception.ResourceNotFoundExceptionLibrary;
import com.mylibrary.exception.ServiceUnavailableExceptionLibrary;
import com.mylibrary.mapper.BookMapper;
import com.mylibrary.metrics.LibraryMetrics;
import com.mylibrary.repository.BookLoanRepository;
import com.mylibrary.repository.BookRepository;
import com.mylibrary.retry.RetryOnDatabaseError;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Recover;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BookService {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");
    private final BookRepository bookRepository;
    private final BookLoanRepository bookLoanRepository;
    private final LibraryMetrics metrics;

    @Autowired
    public BookService(BookRepository bookRepository, BookLoanRepository bookLoanRepository, LibraryMetrics metrics) {
        this.bookRepository = bookRepository;
        this.bookLoanRepository = bookLoanRepository;
        this.metrics = metrics;
    }

    @RetryOnDatabaseError
    public Page<BookDTO> getBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(BookMapper::toBookDTO);
    }

    @RetryOnDatabaseError
    public BookDTO getBookById(Long id) {
        log.info("Searching for book with id {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book id {} not found", id);
                    return new ResourceNotFoundExceptionLibrary("Book with id: " + id + " not found");
                });
        log.debug("Retrieved book id {} successfully", id);
        return BookMapper.toBookDTO(book);
    }

    @RetryOnDatabaseError
    public Page<BookDTO> searchBooks(String title,
                                     String author,
                                     String isbn,
                                     Integer yearFrom,
                                     Integer yearTo,
                                     Boolean availableOnly,
                                     Pageable pageable) {
        log.info("Searching books: title={}, author={}, isbn={}, yearFrom={}, yearTo={}, availableOnly={}",
                title, author, isbn, yearFrom, yearTo, availableOnly);
        String titleParam  = (title  != null && !title.isBlank())  ? title.trim()  : null;
        String authorParam = (author != null && !author.isBlank()) ? author.trim() : null;
        String isbnParam   = (isbn   != null && !isbn.isBlank())   ? isbn.trim()   : null;
        return bookRepository.searchBooks(titleParam, authorParam, isbnParam,
                        yearFrom, yearTo, availableOnly, pageable).map(BookMapper::toBookDTO);
    }

    @Transactional
    public BookDTO addNewBook(NewBookPayload newBook) {
        String newIsbn = newBook.isbn().trim();
        log.info("Creating new book with ISBN: '{}'", newIsbn);
        if (bookRepository.existsByIsbn(newIsbn)) {
            log.warn("Book creation failed. ISBN '{}' already exists", newIsbn);
            throw new ResourceConflictExceptionLibrary("Book with ISBN: " + newIsbn + " already exists");
        }
        Book book = new Book(
                newBook.title().trim(),
                newBook.author().trim(),
                newIsbn,
                newBook.publicationYear(),
                newBook.totalCopies()
        );
        Book saved = bookRepository.save(book);
        metrics.recordBookCreated();
        AUDIT.info("BOOK_CREATED bookId={} isbn={} title=\"{}\" author=\"{}\" totalCopies={}",
                saved.getId(), saved.getIsbn(), saved.getTitle(), saved.getAuthor(), saved.getTotalCopies());
        log.info("Book created successfully with id: {}", saved.getId());
        return BookMapper.toBookDTO(saved);
    }

    @RetryOnDatabaseError
    @Transactional
    public void deleteBookById(Long id) {
        log.info("Deleting book id {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book id {} not found for deletion", id);
                    return new ResourceNotFoundExceptionLibrary("Book with id: " + id + " not found");
                });
        long activeLoans = bookLoanRepository.countByBookIdAndLoanStatus(id, BookLoanStatus.ACTIVE);
        if (activeLoans > 0) {
            log.warn("Cannot delete book id {} — {} active loans exist", id, activeLoans);
            throw new ResourceConflictExceptionLibrary(
                    "Cannot delete book with id:" + id + " because it has " + activeLoans + " active loans");
        }
        bookRepository.delete(book);
        metrics.recordBookDeleted();
        AUDIT.info("BOOK_DELETED bookId={} isbn={} title=\"{}\" deletedBy={}",
                id, book.getIsbn(), book.getTitle(), SecurityContextHolder.getContext().getAuthentication().getName());
        log.info("Book id {} deleted successfully", id);
    }

    @RetryOnDatabaseError
    @Transactional
    public BookDTO updateBookById(Long id, UpdateBookPayload update) {
        log.info("Updating book with id {} with new ISBN: {}", id, update.isbn());
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book with id {} not found for update", id);
                    return new ResourceNotFoundExceptionLibrary("Book with id: " + id + " not found");
                });
        String newIsbn = update.isbn().trim();
        if (!newIsbn.equals(book.getIsbn()) && bookRepository.existsByIsbn(newIsbn)) {
            log.warn("Update failed. Book with ISBN {} already exists", newIsbn);
            throw new ResourceConflictExceptionLibrary("Book with ISBN: " + newIsbn + " already exists");
        }
        book.setTitle(update.title().trim());
        book.setAuthor(update.author().trim());
        book.setIsbn(newIsbn);
        book.setPublicationYear(update.publicationYear());
        int loanedCopies = book.getTotalCopies() - book.getAvailableCopies();
        book.setTotalCopies(update.totalCopies());
        book.setAvailableCopies(update.totalCopies() - loanedCopies);
        if (book.getAvailableCopies() < 0) {
            log.warn("Cannot set total copies to {} because {} copies are currently loaned",
                    update.totalCopies(), loanedCopies);
            throw new ResourceConflictExceptionLibrary(
                    "Cannot set total copies to %s because %s copies are currently loaned"
                            .formatted(update.totalCopies(), loanedCopies));
        }
        log.info("Book with id {} updated successfully", id);
        return BookMapper.toBookDTO(book);
    }

    @Recover
    public Page<BookDTO> recoverGetBooksError(TransientDataAccessException ex, Pageable pageable) {
        log.error("Failed to find books after retries: {}", ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary("Unable to get books. Please try again later.");
    }

    @Recover
    public BookDTO recoverFromGetBookByIdError(TransientDataAccessException ex, Long id) {
        log.error("Failed to get book with id {} after retries: {}", id, ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary("Unable to get book. Please try again later.");
    }

    @Recover
    public Page<BookDTO> recoverFromSearchBooksError(TransientDataAccessException ex,
                                                     String title,
                                                     String author,
                                                     String isbn,
                                                     Integer yearFrom,
                                                     Integer yearTo,
                                                     Boolean availableOnly,
                                                     Pageable pageable) {
        log.error("Failed to search books after retries: {}", ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary("Unable to search books. Please try again later.");
    }

    @Recover
    public void recoverFromDeleteBookByIdError(TransientDataAccessException ex, Long id) {
        log.error("Failed to delete book with id {} after retries: {}", id, ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary(
                "Unable to delete book. Please try again later.");
    }

    @Recover
    public BookDTO recoverFromUpdateBookByIdError(TransientDataAccessException ex, Long id, UpdateBookPayload update) {
        log.error("Failed to update book with id {} after retries: {}", id, ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary(
                "Unable to update book. Please try again later.");
    }
}
