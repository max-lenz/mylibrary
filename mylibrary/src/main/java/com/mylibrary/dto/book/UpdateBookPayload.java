package com.mylibrary.dto.book;

import jakarta.validation.constraints.*;

public record UpdateBookPayload(
        @NotBlank(message = "Title field cannot be empty")
        String title,

        @NotBlank(message = "Author field cannot be empty")
        String author,

        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^\\d{9}[\\dX]$|^\\d{13}$", message = "ISBN must contain 10 or 13 symbols")
        String isbn,

        @NotNull(message = "Publication year is required")
        @Min(value = 1450, message = "Publication year must be after 1450")
        @Max(value = 2100, message = "Publication year must be before 2100")
        Integer publicationYear,

        @NotNull(message = "Total copies number is required")
        @Min(value = 1, message = "Minimum 1 copy required")
        Integer totalCopies) {
}
