package com.mylibrary.dto.book;

import jakarta.validation.constraints.*;

public record NewBookPayload(
        @NotBlank(message = "Title is required")
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String title,

        @NotBlank(message = "Author is required")
        @Size(min = 1, max = 255, message = "Author must be between 1 and 255 characters")
        String author,

        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^(?:\\d{9}[\\dX]|\\d{13})$",
                message = "ISBN must be 10 digits (last may be X) or 13 digits")
        String isbn,

        @NotNull(message = "Publication year is required")
        @Min(value = 1450, message = "Publication year must be after 1450")
        @Max(value = 2100, message = "Publication year must be before 2100")
        Integer publicationYear,

        @NotNull(message = "Total copies number is required")
        @Min(value = 1, message = "Minimum 1 copy required")
        Integer totalCopies) {
}
