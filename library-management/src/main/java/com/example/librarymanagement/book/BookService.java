package com.example.librarymanagement.book;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> list(String keyword, String category, String status) {
        return bookRepository.search(keyword, category)
                .stream()
                .filter(book -> filterByStatus(book, status))
                .toList();
    }

    public Book get(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("图书不存在"));
    }

    @Transactional
    public Book create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new IllegalArgumentException("ISBN 已存在");
        }

        Book book = new Book();
        applyCreateRequest(book, request);
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, BookRequest request) {
        Book book = get(id);
        bookRepository.findByIsbn(request.isbn())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("ISBN 已存在");
                });

        int newTotal = normalizedTotal(request.totalCopies());
        int borrowed = book.getTotalCopies() - book.getAvailableCopies();
        if (newTotal < borrowed) {
            throw new IllegalArgumentException("馆藏数量不能小于已借出数量");
        }

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setPublisher(request.publisher());
        book.setCategory(request.category());
        book.setLocation(request.location());
        book.setPublishDate(request.publishDate());
        book.setDescription(request.description());
        book.setTotalCopies(newTotal);
        book.setAvailableCopies(newTotal - borrowed);
        return bookRepository.save(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = get(id);
        if (!book.getTotalCopies().equals(book.getAvailableCopies())) {
            throw new IllegalArgumentException("该图书仍有未归还记录，不能删除");
        }
        bookRepository.delete(book);
    }

    private void applyCreateRequest(Book book, BookRequest request) {
        int total = normalizedTotal(request.totalCopies());
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setPublisher(request.publisher());
        book.setCategory(request.category());
        book.setLocation(request.location());
        book.setPublishDate(request.publishDate());
        book.setDescription(request.description());
        book.setTotalCopies(total);
        book.setAvailableCopies(total);
    }

    private int normalizedTotal(Integer totalCopies) {
        return totalCopies == null ? 1 : totalCopies;
    }

    private boolean filterByStatus(Book book, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return switch (status.toLowerCase()) {
            case "available" -> book.getAvailableCopies() > 0;
            case "unavailable" -> book.getAvailableCopies() == 0;
            default -> true;
        };
    }
}
