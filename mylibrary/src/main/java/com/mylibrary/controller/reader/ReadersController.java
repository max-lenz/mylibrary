package com.mylibrary.controller.reader;

import com.mylibrary.dto.reader.NewReaderPayload;
import com.mylibrary.dto.reader.ReaderDTO;
import com.mylibrary.service.ReaderService;
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
@RequestMapping("api/readers")
public class ReadersController {
    private final ReaderService readerService;

    @Autowired
    public ReadersController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @GetMapping
    public Page<ReaderDTO> getAllReaders(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(defaultValue = "ASC") Sort.Direction direction,
                                         @RequestParam(defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, direction, sortBy);
        return readerService.getReaders(pageable);
    }

    @PostMapping
    public ResponseEntity<ReaderDTO> addReader(@Valid @RequestBody NewReaderPayload newReader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(readerService.addNewReader(newReader));
    }
}