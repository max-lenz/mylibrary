package com.mylibrary.service;

import com.mylibrary.dto.reader.NewReaderPayload;
import com.mylibrary.dto.reader.ReaderDTO;
import com.mylibrary.dto.reader.UpdateReaderPayload;
import com.mylibrary.entity.BookLoanStatus;
import com.mylibrary.entity.Reader;
import com.mylibrary.exception.ResourceConflictExceptionLibrary;
import com.mylibrary.exception.ResourceNotFoundExceptionLibrary;
import com.mylibrary.exception.ServiceUnavailableExceptionLibrary;
import com.mylibrary.mapper.ReaderMapper;
import com.mylibrary.metrics.LibraryMetrics;
import com.mylibrary.repository.BookLoanRepository;
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

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReaderService {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final ReaderRepository readerRepository;
    private final BookLoanRepository bookLoanRepository;
    private final LibraryMetrics metrics;

    @Autowired
    public ReaderService(ReaderRepository readerRepository, BookLoanRepository bookLoanRepository, LibraryMetrics metrics) {
        this.readerRepository = readerRepository;
        this.bookLoanRepository = bookLoanRepository;
        this.metrics = metrics;
    }

    @RetryOnDatabaseError
    public Page<ReaderDTO> getReaders(Pageable pageable) {
        log.info("Getting all readers");
        return readerRepository.findAll(pageable).map(ReaderMapper::toReaderDTO);
    }

    @RetryOnDatabaseError
    public ReaderDTO getReaderById(Long id) {
        log.info("Searching for reader with id {}", id);
        Reader reader = readerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Reader id {} not found", id);
                    return new ResourceNotFoundExceptionLibrary("Reader with id:" + id + " not found");
                });
        log.debug("Retrieved reader id {} successfully", id);
        return ReaderMapper.toReaderDTO(reader);
    }

    @Transactional
    public ReaderDTO addNewReader(NewReaderPayload newReader) {
        String newEmail = newReader.email().trim().toLowerCase();
        String newPhone = newReader.phone().trim();
        log.info("Registering new reader with email: {}", maskEmail(newEmail));
        if (readerRepository.existsByEmail(newEmail)) {
            log.warn("Reader registration failed. Reader with email: {} already exists", maskEmail(newEmail));
            throw new ResourceConflictExceptionLibrary("Reader with email:" + newEmail + " already exists");
        }
        if (readerRepository.existsByPhone(newPhone)) {
            log.warn("Reader registration failed. Reader with phone: {} already exists", maskPhone(newPhone));
            throw new ResourceConflictExceptionLibrary("Reader with phone number:" + newPhone + " already exists");
        }
        Reader reader = new Reader(
                newReader.firstName().trim(),
                newReader.lastName().trim(),
                newEmail,
                newPhone);
        Reader saved = readerRepository.save(reader);
        metrics.recordReaderRegistered();
        AUDIT.info("READER_REGISTERED readerId={} firstName=\"{}\" lastName=\"{}\" email={} phone={}",
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                maskEmail(saved.getEmail()),
                maskPhone(saved.getPhone()));
        log.info("Reader registered successfully with id: {}", saved.getId());
        return ReaderMapper.toReaderDTO(saved);
    }

    @RetryOnDatabaseError
    @Transactional
    public void deleteReaderById(Long id) {
        log.info("Deleting reader id {}", id);
        Reader reader = readerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Reader id {} not found for deletion", id);
                    return new ResourceNotFoundExceptionLibrary("Reader with id:" + id + " not found");
                });
        long activeLoans = bookLoanRepository.countByReaderIdAndLoanStatus(id, BookLoanStatus.ACTIVE);
        if (activeLoans > 0) {
            log.warn("Cannot delete reader id {} — {} active loans exist", id, activeLoans);
            throw new ResourceConflictExceptionLibrary(
                    "Cannot delete reader with id:" + id + " because they have " + activeLoans + " active loans");
        }
        readerRepository.delete(reader);
        AUDIT.info("READER_DELETED readerId={}", id);
        log.info("Reader id {} deleted successfully", id);
    }

    @RetryOnDatabaseError
    @Transactional
    public ReaderDTO updateReaderById(Long id, UpdateReaderPayload update) {
        log.info("Updating reader with id {}", id);
        String updateEmail = update.email().trim().toLowerCase();
        String updatePhone = update.phone().trim();
        Reader reader = readerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Reader with id {} not found for update", id);
                    return new ResourceNotFoundExceptionLibrary("Reader with id:" + id + " not found");
                });
        if (!reader.getEmail().equals(updateEmail)) {
            if (readerRepository.existsByEmail(updateEmail)) {
                log.warn("Update failed. Reader with email {} already exists", maskEmail(updateEmail));
                throw new ResourceConflictExceptionLibrary("Reader with email:" + updateEmail + " already exists");
            }
        }
        if (!reader.getPhone().equals(updatePhone)) {
            if (readerRepository.existsByPhone(updatePhone)) {
                log.warn("Update failed. Reader with phone {} already exists", maskPhone(updatePhone));
                throw new ResourceConflictExceptionLibrary("Reader with phone number:" + updatePhone + " already exists");
            }
        }
        reader.setFirstName(update.firstName().trim());
        reader.setLastName(update.lastName().trim());
        reader.setEmail(updateEmail);
        reader.setPhone(updatePhone);
        log.info("Reader with id {} updated successfully", id);
        return ReaderMapper.toReaderDTO(reader);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***@***";
        String[] parts = email.split("@");
        return parts[0].substring(0, Math.min(2, parts[0].length())) + "***@" + parts[1];
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return "***" + phone.substring(phone.length() - 4);
    }

    @Recover
    public Page<ReaderDTO> recoverFromGetReadersError(TransientDataAccessException ex, Pageable pageable) {
        log.error("Failed to find readers after retries: {}", ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary("Unable to get readers. Please try again later.");
    }

    @Recover
    public ReaderDTO recoverFromGetReaderByIdError(TransientDataAccessException ex, Long id) {
        log.error("Failed to get reader with id {} after retries: {}", id, ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary("Unable to get reader. Please try again later.");
    }

    @Recover
    public void recoverFromDeleteReaderByIdError(TransientDataAccessException ex, Long id) {
        log.error("Failed to delete reader with id {} after retries: {}", id, ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary(
                "Unable to delete reader. Please try again later.");
    }

    @Recover
    public ReaderDTO recoverFromUpdateReaderByIdError(TransientDataAccessException ex, Long id, UpdateReaderPayload update) {
        log.error("Failed to update reader with id {} after retries: {}", id, ex.getMessage());
        throw new ServiceUnavailableExceptionLibrary(
                "Unable to update reader. Please try again later.");
    }
}
