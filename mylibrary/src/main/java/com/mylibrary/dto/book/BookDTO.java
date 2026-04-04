package com.mylibrary.dto.book;

public record BookDTO(Long id,
                      String title,
                      String author,
                      String isbn,
                      Integer publicationYear,
                      Integer totalCopies,
                      Integer availableCopies) {
}
