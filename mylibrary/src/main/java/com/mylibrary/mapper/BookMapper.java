package com.mylibrary.mapper;

import com.mylibrary.dto.book.BookDTO;
import com.mylibrary.entity.Book;

public class BookMapper {
    public static BookDTO toBookDTO(Book book) {
        return new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.getTotalCopies(),
                book.getAvailableCopies());
    }
}
