package com.mylibrary.repository;

import com.mylibrary.entity.BookLoan;
import com.mylibrary.entity.BookLoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {

    @EntityGraph(attributePaths = {"book", "reader"})
    Optional<BookLoan> findByBookIdAndReaderIdAndLoanStatus(Long bookId,
                                                            Long readerId,
                                                            BookLoanStatus bookLoanStatus);

    @EntityGraph(attributePaths = {"book"})
    Optional<BookLoan> findBookLoanById(Long id);

    @EntityGraph(attributePaths = {"book", "reader"})
    Page<BookLoan> findAllBookLoansByReaderId(Long readerId, Pageable pageable);

    @EntityGraph(attributePaths = {"book", "reader"})
    Page<BookLoan> findByLoanStatusAndDueDateBefore(BookLoanStatus bookLoanStatus, LocalDate dueDate,
                                                    Pageable pageable);

    @EntityGraph(attributePaths = {"book", "reader"})
    Page<BookLoan> findByLoanStatus(BookLoanStatus bookLoanStatus, Pageable pageable);

    long countByLoanStatus(BookLoanStatus bookLoanStatus);

    long countByLoanStatusAndDueDateBefore(BookLoanStatus bookLoanStatus, LocalDate dueDate);

    long countByBookIdAndLoanStatus(Long id, BookLoanStatus bookLoanStatus);

    long countByReaderIdAndLoanStatus(Long id, BookLoanStatus bookLoanStatus);
}
