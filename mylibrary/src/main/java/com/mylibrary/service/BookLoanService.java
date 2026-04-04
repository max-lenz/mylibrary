package com.mylibrary.service;

import com.mylibrary.dto.bookloan.BookLoanDTO;
import com.mylibrary.dto.bookloan.NewBookLoanPayload;
import com.mylibrary.entity.Book;
import com.mylibrary.entity.BookLoan;
import com.mylibrary.entity.BookLoanStatus;
import com.mylibrary.entity.Reader;
import com.mylibrary.exception.*;
import com.mylibrary.exception.ResourceConflictExceptionLibrary;
import com.mylibrary.exception.ResourceNotFoundExceptionLibrary;
import com.mylibrary.mapper.BookLoanMapper;
import com.mylibrary.metrics.LibraryMetrics;
import com.mylibrary.repository.BookLoanRepository;
import com.mylibrary.repository.BookRepository;
import com.mylibrary.repository.ReaderRepository;
import com.mylibrary.retry.RetryOnDatabaseError;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BookLoanService {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final BookLoanRepository bookLoanRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final LibraryMetrics metrics;

    @Autowired
    public BookLoanService(BookLoanRepository bookLoanRepository,
                           BookRepository bookRepository,
                           ReaderRepository readerRepository,
                           LibraryMetrics metrics) {
        this.bookLoanRepository = bookLoanRepository;
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
        this.metrics = metrics;
    }

    @Transactional
    public BookLoanDTO addNewLoan(NewBookLoanPayload newBookLoan) {
        log.info("Issuing book loan: bookId={}, readerId={}", newBookLoan.bookId(), newBookLoan.readerId());
        Book book = bookRepository.findById(newBookLoan.bookId())
                .orElseThrow(() -> {
                    log.warn("Book id {} not found for loan", newBookLoan.bookId());
                    return new ResourceNotFoundExceptionLibrary(
                            "Book with id:" + newBookLoan.bookId() + " not found");
                });
        if (!readerRepository.existsById(newBookLoan.readerId())) {
            log.warn("Reader id {} not found for loan", newBookLoan.readerId());
            throw new ResourceNotFoundExceptionLibrary(
                    "Reader with id:" + newBookLoan.readerId() + " not found");
        }
        Reader reader = readerRepository.getReferenceById(newBookLoan.readerId());
        Optional<BookLoan> existingLoan = bookLoanRepository.findByBookIdAndReaderIdAndLoanStatus(
                book.getId(),
                reader.getId(),
                BookLoanStatus.ACTIVE);
        if (existingLoan.isPresent()) {
            log.warn("Reader {} already has active loan for book {}", reader.getId(), book.getId());
            throw new ResourceConflictExceptionLibrary(
                    "Reader with id:" + reader.getId() +
                            " already has active loan for book id:" + book.getId());
        }
        if (book.getAvailableCopies() <= 0) {
            log.warn("No available copies for book id {}", book.getId());
            throw new ResourceConflictExceptionLibrary(
                    "No available copies of book with id:" + book.getId());
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        if (newBookLoan.dueDate().isAfter(LocalDate.now().plusDays(60))) {
            throw new InvalidInputExceptionLibrary(
                    "Due date cannot be more than 60 days from today");
        }
        BookLoan bookLoan = new BookLoan(
                book,
                reader,
                newBookLoan.dueDate());
        BookLoan saved = bookLoanRepository.save(bookLoan);
        metrics.recordBookLoaned();
        AUDIT.info("BOOK_ISSUED loanId={} bookId={} readerId={} dueDate={}",
                saved.getId(), book.getId(), reader.getId(), saved.getDueDate());
        log.info("Book loan issued successfully: loanId={}", saved.getId());
        return BookLoanMapper.toBookLoanDTO(saved);
    }

    @RetryOnDatabaseError
    @Transactional
    public BookLoanDTO returnBookByLoanId(Long id) {
        log.info("Returning book for loan id {}", id);
        BookLoan bookLoan = bookLoanRepository.findBookLoanById(id)
                .orElseThrow(() -> {
                    log.warn("Loan id {} not found for return", id);
                    return new ResourceNotFoundExceptionLibrary(
                            "Loan with id:" + id + " not found");
                });
        if (bookLoan.getLoanStatus() != BookLoanStatus.ACTIVE) {
            log.warn("Loan id {} is not active, cannot return", id);
            throw new ResourceConflictExceptionLibrary(
                    "Only active loans can be marked as returned");
        }
        Book book = bookLoan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookLoan.setReturnDate(LocalDate.now());
        bookLoan.setLoanStatus(BookLoanStatus.RETURNED);
        metrics.recordBookReturned();
        boolean wasOverdue = LocalDate.now().isAfter(bookLoan.getDueDate());
        if (wasOverdue) {
            metrics.recordOverdueLoan();
            log.warn("Book returned with overdue: loanId={}", id);
        }
        AUDIT.info("BOOK_RETURNED loanId={} bookId={} readerId={} returnDate={} wasOverdue={}",
                id, book.getId(), bookLoan.getReader().getId(), bookLoan.getReturnDate(), wasOverdue);
        log.info("Book returned successfully: loanId={}", id);
        return BookLoanMapper.toBookLoanDTO(bookLoan);
    }

    @RetryOnDatabaseError
    public Page<BookLoanDTO> getFullReaderHistory(Long readerId, Pageable pageable) {
        log.info("Getting full history for reader id {}", readerId);
        if (!readerRepository.existsById(readerId)) {
            log.warn("Reader id {} not found for history", readerId);
            throw new ResourceNotFoundExceptionLibrary(
                    "Reader with id:" + readerId + " not found");
        }
        return bookLoanRepository.findAllBookLoansByReaderId(readerId, pageable)
                .map(BookLoanMapper::toBookLoanDTO);
    }

    @RetryOnDatabaseError
    public Page<BookLoanDTO> getAllOverdueLoans(Pageable pageable) {
        log.info("Getting all overdue loans");
        return bookLoanRepository.findByLoanStatusAndDueDateBefore(BookLoanStatus.ACTIVE, LocalDate.now(), pageable)
                .map(BookLoanMapper::toBookLoanDTO);
    }

    @RetryOnDatabaseError
    public Page<BookLoanDTO> getAllLostLoans(Pageable pageable) {
        log.info("Getting all loans with LOST status");
        return bookLoanRepository.findByLoanStatus(BookLoanStatus.LOST, pageable)
                .map(BookLoanMapper::toBookLoanDTO);
    }

    @RetryOnDatabaseError
    @Transactional
    public BookLoanDTO renewBookLoan(Long id, Integer days) {
        if (days <= 0) {
            throw new InvalidInputExceptionLibrary(
                    "Renewal period must be at least 1 day");
        }
        if (days > 30) {
            throw new InvalidInputExceptionLibrary(
                    "Renew date cannot be more than 30 days from today");
        }
        log.info("Renewing book loan id {} for {} days", id, days);
        BookLoan bookLoan = bookLoanRepository.findBookLoanById(id)
                .orElseThrow(() -> {
                    log.warn("Loan id {} not found for renewal", id);
                    return new ResourceNotFoundExceptionLibrary(
                            "Loan with id:" + id + " not found");
                });
        if (bookLoan.getLoanStatus() != BookLoanStatus.ACTIVE) {
            log.warn("Loan id {} is not active, cannot renew", id);
            throw new ResourceConflictExceptionLibrary(
                    "Only active loans can be renewed");
        }
        if (LocalDate.now().isAfter(bookLoan.getDueDate())) {
            log.warn("Loan id {} is overdue, cannot renew", id);
            throw new ResourceConflictExceptionLibrary(
                    "Cannot renew overdue loan");
        }
        LocalDate oldDueDate = bookLoan.getDueDate();
        bookLoan.setDueDate(bookLoan.getDueDate().plusDays(days));
        AUDIT.info("BOOK_RENEWED loanId={} bookId={} readerId={} oldDueDate={} newDueDate={}",
                id, bookLoan.getBook().getId(), bookLoan.getReader().getId(),
                oldDueDate, bookLoan.getDueDate());
        log.info("Book loan id {} renewed successfully", id);
        return BookLoanMapper.toBookLoanDTO(bookLoan);
    }

    @RetryOnDatabaseError
    @Transactional
    public BookLoanDTO markAsLostByLoanId(Long id) {
        log.info("Marking book as lost for loan id {}", id);
        BookLoan bookLoan = bookLoanRepository.findBookLoanById(id)
                .orElseThrow(() -> {
                    log.warn("Loan id {} not found for marking as lost", id);
                    return new ResourceNotFoundExceptionLibrary(
                            "Loan with id:" + id + " not found");
                });
        if (bookLoan.getLoanStatus() != BookLoanStatus.ACTIVE) {
            log.warn("Loan id {} is not active, cannot mark as lost", id);
            throw new ResourceConflictExceptionLibrary(
                    "Only active loans can be marked as lost");
        }
        Book book = bookLoan.getBook();
        if (book.getTotalCopies() <= 0) {
            throw new ResourceConflictExceptionLibrary(
                    "Cannot mark as lost: book id:" + book.getId() +
                            " already has 0 total copies");
        }
        book.setTotalCopies(book.getTotalCopies() - 1);
        validateCopyInvariants(book);
        bookLoan.setLoanStatus(BookLoanStatus.LOST);
        AUDIT.info("BOOK_LOST loanId={} bookId={} readerId={} remainingCopies={}",
                id, book.getId(), bookLoan.getReader().getId(), book.getTotalCopies());
        log.info("Book marked as lost for loan id {}", id);
        return BookLoanMapper.toBookLoanDTO(bookLoan);
    }

    private void validateCopyInvariants(Book book) {
        int total = book.getTotalCopies();
        int available = book.getAvailableCopies();

        if (total < 0) {
            throw new ResourceConflictExceptionLibrary(
                    "Invariant violated: totalCopies < 0 for book id:" + book.getId());
        }
        if (available < 0) {
            throw new ResourceConflictExceptionLibrary(
                    "Invariant violated: availableCopies < 0 for book id:" + book.getId());
        }
        if (available > total) {
            throw new ResourceConflictExceptionLibrary(
                    "Invariant violated: availableCopies(" + available +
                            ") > totalCopies(" + total + ") for book id:" + book.getId());
        }
    }

    @Recover
    public BookLoanDTO recoverFromReturnBookByLoanIdError(TransientDataAccessException ex, Long id) {
        log.error("Failed to return book by loan with id {} after retries: {}", id, ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary("Unable to return book by loan. Please try again later.");
    }

    @Recover
    public Page<BookLoanDTO> recoverFromGetFullReaderHistoryError(TransientDataAccessException ex, Long readerId, Pageable pageable) {
        log.error("Failed to get full reader history for reader {} after retries: {}", readerId, ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary("Unable to get full reader history. Please try again later.");
    }

    @Recover
    public Page<BookLoanDTO> recoverFromGetAllOverdueLoansError(TransientDataAccessException ex, Pageable pageable) {
        log.error("Failed to get all overdue loans after retries: {}", ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary("Unable to get all overdue loans. Please try again later.");
    }

    @Recover
    public BookLoanDTO recoverFromRenewBookLoanError(TransientDataAccessException ex, Long id, Integer days) {
        log.error("Failed to renew book loan with id {} for {} days after retries: {}", id, days, ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary("Unable to renew book loan. Please try again later.");
    }

    @Recover
    public BookLoanDTO recoverFromMarkAsLostByLoanIdError(TransientDataAccessException ex, Long id) {
        log.error("Failed to mark book as lost by loan with id {} after retries: {}", id, ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary("Unable to mark book as lost. Please try again later.");
    }
}
