package com.example.librarymanagement.reader;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ReaderRequest(
        @NotBlank String name,
        String gender,
        String phone,
        @Email String email,
        String department,
        @NotBlank String cardNumber,
        LocalDate registeredDate,
        ReaderStatus status
) {
}
