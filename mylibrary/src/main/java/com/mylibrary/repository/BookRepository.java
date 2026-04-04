package com.mylibrary.repository;

import com.mylibrary.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    @Query("""
        SELECT b FROM Book b
        WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))
          AND (:isbn IS NULL OR b.isbn = :isbn)
          AND (:yearFrom IS NULL OR b.publicationYear >= :yearFrom)
          AND (:yearTo IS NULL OR b.publicationYear <= :yearTo)
          AND (:availableOnly IS NULL OR :availableOnly = false OR b.availableCopies > 0)
        """)
    Page<Book> searchBooks(
            @Param("title") String title,
            @Param("author") String author,
            @Param("isbn") String isbn,
            @Param("yearFrom") Integer yearFrom,
            @Param("yearTo") Integer yearTo,
            @Param("availableOnly") Boolean availableOnly,
            Pageable pageable);
}

