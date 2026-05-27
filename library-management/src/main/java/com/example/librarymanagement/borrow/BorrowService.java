package com.example.librarymanagement.borrow;

import com.example.librarymanagement.book.Book;
import com.example.librarymanagement.book.BookService;
import com.example.librarymanagement.reader.Reader;
import com.example.librarymanagement.reader.ReaderService;
import com.example.librarymanagement.reader.ReaderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowService {
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookService bookService;
    private final ReaderService readerService;

    public BorrowService(BorrowRecordRepository borrowRecordRepository,
                         BookService bookService,
                         ReaderService readerService) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookService = bookService;
        this.readerService = readerService;
    }

    public List<BorrowRecord> list(String keyword, BorrowStatus status) {
        return borrowRecordRepository.search(keyword, status);
    }

    public BorrowRecord get(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("借阅记录不存在"));
    }

    @Transactional
    public BorrowRecord borrow(BorrowRequest request) {
        Book book = bookService.get(request.bookId());
        Reader reader = readerService.get(request.readerId());

        if (reader.getStatus() != ReaderStatus.ACTIVE) {
            throw new IllegalArgumentException("读者状态不可借阅");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalArgumentException("该图书暂无可借副本");
        }

        LocalDate borrowDate = request.borrowDate() == null ? LocalDate.now() : request.borrowDate();
        LocalDate dueDate = request.dueDate() == null ? borrowDate.plusDays(30) : request.dueDate();
        if (dueDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("应还日期不能早于借阅日期");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        BorrowRecord record = new BorrowRecord();
        record.setBook(book);
        record.setReader(reader);
        record.setBorrowDate(borrowDate);
        record.setDueDate(dueDate);
        record.setStatus(BorrowStatus.BORROWED);
        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord returnBook(Long id, ReturnRequest request) {
        BorrowRecord record = get(id);
        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new IllegalArgumentException("该借阅记录已归还");
        }

        LocalDate returnDate = request == null || request.returnDate() == null
                ? LocalDate.now()
                : request.returnDate();
        if (returnDate.isBefore(record.getBorrowDate())) {
            throw new IllegalArgumentException("归还日期不能早于借阅日期");
        }

        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        record.setReturnDate(returnDate);
        record.setStatus(BorrowStatus.RETURNED);
        return borrowRecordRepository.save(record);
    }

    @Transactional
    public void delete(Long id) {
        BorrowRecord record = get(id);
        if (record.getStatus() == BorrowStatus.BORROWED) {
            Book book = record.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
        }
        borrowRecordRepository.delete(record);
    }

    public long countActiveBorrows() {
        return borrowRecordRepository.countByStatus(BorrowStatus.BORROWED);
    }

    public long countOverdue() {
        LocalDate today = LocalDate.now();
        return borrowRecordRepository.search(null, BorrowStatus.BORROWED)
                .stream()
                .filter(record -> record.getDueDate().isBefore(today))
                .count();
    }
}
