package com.mylibrary.controller.reader;

import com.mylibrary.dto.reader.ReaderDTO;
import com.mylibrary.dto.reader.UpdateReaderPayload;
import com.mylibrary.service.ReaderService;
import com.mylibrary.validation.ValidId;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("api/readers/{id}")
public class ReaderController {
    private final ReaderService readerService;

    @Autowired
    public ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @GetMapping
    public ReaderDTO getReader(@ValidId @PathVariable Long id) {
        return readerService.getReaderById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReader(@ValidId @PathVariable Long id) {
        readerService.deleteReaderById(id);
    }

    @PutMapping
    public ReaderDTO updateReader(@ValidId @PathVariable Long id,
                                  @Valid @RequestBody UpdateReaderPayload newReader) {
     return readerService.updateReaderById(id, newReader);
    }
}