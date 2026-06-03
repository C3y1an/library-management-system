package com.example.librarymanagement.borrow;

import com.example.librarymanagement.book.Book;
import com.example.librarymanagement.book.BookRequest;
import com.example.librarymanagement.book.BookService;
import com.example.librarymanagement.reader.Reader;
import com.example.librarymanagement.reader.ReaderRequest;
import com.example.librarymanagement.reader.ReaderService;
import com.example.librarymanagement.reader.ReaderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BorrowServiceTests {

    @Autowired
    private BookService bookService;

    @Autowired
    private ReaderService readerService;

    @Autowired
    private BorrowService borrowService;

    @Test
    void borrowBookReducesAvailableCopiesAndCreatesActiveRecord() {
        Book book = createBook("ISBN-BORROW-001", 2);
        Reader reader = createReader("CARD-BORROW-001", ReaderStatus.ACTIVE);

        BorrowRecord record = borrowService.borrow(new BorrowRequest(
                book.getId(),
                reader.getId(),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 1)
        ));

        assertThat(record.getStatus()).isEqualTo(BorrowStatus.BORROWED);
        assertThat(record.getBorrowDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(record.getDueDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(bookService.get(book.getId()).getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void returnBookRestoresAvailableCopiesAndStoresReturnDate() {
        Book book = createBook("ISBN-RETURN-001", 1);
        Reader reader = createReader("CARD-RETURN-001", ReaderStatus.ACTIVE);
        BorrowRecord record = borrowService.borrow(new BorrowRequest(
                book.getId(),
                reader.getId(),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 1)
        ));

        BorrowRecord returned = borrowService.returnBook(
                record.getId(),
                new ReturnRequest(LocalDate.of(2026, 6, 10))
        );

        assertThat(returned.getStatus()).isEqualTo(BorrowStatus.RETURNED);
        assertThat(returned.getReturnDate()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(bookService.get(book.getId()).getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void borrowRejectsInactiveReaderNoStockAndInvalidDueDate() {
        Book singleCopyBook = createBook("ISBN-REJECT-001", 1);
        Reader activeReader = createReader("CARD-REJECT-ACTIVE", ReaderStatus.ACTIVE);
        Reader disabledReader = createReader("CARD-REJECT-DISABLED", ReaderStatus.DISABLED);

        assertThatThrownBy(() -> borrowService.borrow(new BorrowRequest(
                singleCopyBook.getId(),
                disabledReader.getId(),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 1)
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("读者状态不可借阅");

        assertThatThrownBy(() -> borrowService.borrow(new BorrowRequest(
                singleCopyBook.getId(),
                activeReader.getId(),
                LocalDate.of(2026, 6, 2),
                LocalDate.of(2026, 6, 1)
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("应还日期不能早于借阅日期");

        borrowService.borrow(new BorrowRequest(
                singleCopyBook.getId(),
                activeReader.getId(),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 1)
        ));

        Reader anotherReader = createReader("CARD-REJECT-ANOTHER", ReaderStatus.ACTIVE);
        assertThatThrownBy(() -> borrowService.borrow(new BorrowRequest(
                singleCopyBook.getId(),
                anotherReader.getId(),
                LocalDate.of(2026, 6, 2),
                LocalDate.of(2026, 7, 2)
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("该图书暂无可借副本");
    }

    @Test
    void duplicateReturnAndInvalidReturnDateAreRejected() {
        Book book = createBook("ISBN-RETURN-REJECT-001", 1);
        Reader reader = createReader("CARD-RETURN-REJECT-001", ReaderStatus.ACTIVE);
        BorrowRecord record = borrowService.borrow(new BorrowRequest(
                book.getId(),
                reader.getId(),
                LocalDate.of(2026, 6, 5),
                LocalDate.of(2026, 7, 5)
        ));

        assertThatThrownBy(() -> borrowService.returnBook(
                record.getId(),
                new ReturnRequest(LocalDate.of(2026, 6, 4))
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("归还日期不能早于借阅日期");

        borrowService.returnBook(record.getId(), new ReturnRequest(LocalDate.of(2026, 6, 8)));

        assertThatThrownBy(() -> borrowService.returnBook(
                record.getId(),
                new ReturnRequest(LocalDate.of(2026, 6, 9))
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("该借阅记录已归还");
    }

    @Test
    void deletingActiveBorrowRecordRestoresAvailableCopies() {
        Book book = createBook("ISBN-DELETE-001", 1);
        Reader reader = createReader("CARD-DELETE-001", ReaderStatus.ACTIVE);
        BorrowRecord record = borrowService.borrow(new BorrowRequest(
                book.getId(),
                reader.getId(),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 1)
        ));

        borrowService.delete(record.getId());

        assertThat(bookService.get(book.getId()).getAvailableCopies()).isEqualTo(1);
        assertThatThrownBy(() -> borrowService.get(record.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("借阅记录不存在");
    }

    private Book createBook(String isbn, int totalCopies) {
        return bookService.create(new BookRequest(
                "测试图书" + isbn,
                "测试作者",
                isbn,
                "测试出版社",
                "软件工程",
                "A-01",
                LocalDate.of(2025, 1, 1),
                totalCopies,
                "测试用图书"
        ));
    }

    private Reader createReader(String cardNumber, ReaderStatus status) {
        return readerService.create(new ReaderRequest(
                "测试读者" + cardNumber,
                "男",
                "13800000000",
                cardNumber.toLowerCase() + "@example.com",
                "计算机学院",
                cardNumber,
                LocalDate.of(2026, 6, 1),
                status
        ));
    }
}
