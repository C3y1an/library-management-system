package com.example.librarymanagement.borrow;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BorrowRequest(
        @NotNull Long bookId,
        @NotNull Long readerId,
        LocalDate borrowDate,
        LocalDate dueDate
) {
}
