package com.mylibrary.dto.reader;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateReaderPayload(
        @NotBlank(message = "First name cannot be empty")
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be empty")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        String lastName,

        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(
                regexp = "^\\+[0-9\\-\\s()]{10,20}$",
                message = "Phone must be international format starting with + and minimum 10 digits"
        )
        String phone) {
}
