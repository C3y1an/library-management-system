package com.example.librarymanagement.reader;

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
@RequestMapping("/api/readers")
public class ReaderController {
    private final ReaderService readerService;

    public ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @GetMapping
    public ApiResponse<List<Reader>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) ReaderStatus status) {
        return ApiResponse.ok(readerService.list(keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<Reader> get(@PathVariable Long id) {
        return ApiResponse.ok(readerService.get(id));
    }

    @PostMapping
    public ApiResponse<Reader> create(@Valid @RequestBody ReaderRequest request) {
        return ApiResponse.ok("读者创建成功", readerService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Reader> update(@PathVariable Long id, @Valid @RequestBody ReaderRequest request) {
        return ApiResponse.ok("读者更新成功", readerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        readerService.delete(id);
        return ApiResponse.ok("读者删除成功", null);
    }
}
