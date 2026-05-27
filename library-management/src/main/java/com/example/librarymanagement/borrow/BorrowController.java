package com.example.librarymanagement.borrow;

import com.example.librarymanagement.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {
    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping
    public ApiResponse<List<BorrowRecord>> list(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) BorrowStatus status) {
        return ApiResponse.ok(borrowService.list(keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<BorrowRecord> get(@PathVariable Long id) {
        return ApiResponse.ok(borrowService.get(id));
    }

    @PostMapping
    public ApiResponse<BorrowRecord> borrow(@Valid @RequestBody BorrowRequest request) {
        return ApiResponse.ok("借阅登记成功", borrowService.borrow(request));
    }

    @PostMapping("/{id}/return")
    public ApiResponse<BorrowRecord> returnBook(@PathVariable Long id,
                                                @RequestBody(required = false) ReturnRequest request) {
        return ApiResponse.ok("图书归还成功", borrowService.returnBook(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        borrowService.delete(id);
        return ApiResponse.ok("借阅记录已删除", null);
    }
}
