package com.example.librarymanagement.book;

import com.example.librarymanagement.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ApiResponse<List<Book>> list(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String status) {
        return ApiResponse.ok(bookService.list(keyword, category, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<Book> get(@PathVariable Long id) {
        return ApiResponse.ok(bookService.get(id));
    }

    @PostMapping
    public ApiResponse<Book> create(@Valid @RequestBody BookRequest request) {
        return ApiResponse.ok("图书创建成功", bookService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Book> update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ApiResponse.ok("图书更新成功", bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ApiResponse.ok("图书删除成功", null);
    }
}
