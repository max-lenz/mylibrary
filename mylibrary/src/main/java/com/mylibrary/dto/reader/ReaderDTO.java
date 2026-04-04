package com.mylibrary.dto.reader;

import java.time.LocalDate;

public record ReaderDTO(Long id,
                        String firstName,
                        String lastName,
                        String email,
                        String phone,
                        LocalDate registrationDate) {
}
