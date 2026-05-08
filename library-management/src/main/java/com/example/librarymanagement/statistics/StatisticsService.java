package com.example.librarymanagement.statistics;

import com.example.librarymanagement.book.BookRepository;
import com.example.librarymanagement.borrow.BorrowService;
import com.example.librarymanagement.reader.ReaderRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StatisticsService {
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final BorrowService borrowService;

    public StatisticsService(BookRepository bookRepository,
                             ReaderRepository readerRepository,
                             BorrowService borrowService) {
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
        this.borrowService = borrowService;
    }

    public Map<String, Object> overview() {
        long totalBooks = bookRepository.count();
        int totalCopies = bookRepository.findAll().stream()
                .mapToInt(book -> book.getTotalCopies() == null ? 0 : book.getTotalCopies())
                .sum();
        int availableCopies = bookRepository.findAll().stream()
                .mapToInt(book -> book.getAvailableCopies() == null ? 0 : book.getAvailableCopies())
                .sum();

        return Map.of(
                "totalBooks", totalBooks,
                "totalCopies", totalCopies,
                "availableCopies", availableCopies,
                "totalReaders", readerRepository.count(),
                "activeBorrows", borrowService.countActiveBorrows(),
                "overdueBorrows", borrowService.countOverdue()
        );
    }
}
