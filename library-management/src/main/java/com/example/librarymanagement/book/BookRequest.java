package com.example.librarymanagement.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record BookRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String isbn,
        String publisher,
        String category,
        String location,
        LocalDate publishDate,
        @Min(1) Integer totalCopies,
        String description
) {
}
