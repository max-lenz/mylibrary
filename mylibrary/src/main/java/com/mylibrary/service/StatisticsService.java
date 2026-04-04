package com.mylibrary.service;

import com.mylibrary.entity.BookLoanStatus;
import com.mylibrary.repository.BookLoanRepository;
import com.mylibrary.repository.BookRepository;
import com.mylibrary.repository.ReaderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final BookLoanRepository bookLoanRepository;

    public StatisticsService(BookRepository bookRepository,
                             ReaderRepository readerRepository,
                             BookLoanRepository bookLoanRepository) {
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
        this.bookLoanRepository = bookLoanRepository;
    }

    public Map<String, Object> getLibraryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks", bookRepository.count());
        stats.put("totalReaders", readerRepository.count());
        stats.put("activeLoans", bookLoanRepository.countByLoanStatus(BookLoanStatus.ACTIVE));
        stats.put("overdueLoans", bookLoanRepository.countByLoanStatusAndDueDateBefore(BookLoanStatus.ACTIVE,
                LocalDate.now()));
        stats.put("lostBooks", bookLoanRepository.countByLoanStatus(BookLoanStatus.LOST));
        return stats;
    }
}
