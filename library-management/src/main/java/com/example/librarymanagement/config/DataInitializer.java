package com.example.librarymanagement.config;

import com.example.librarymanagement.book.BookRequest;
import com.example.librarymanagement.book.BookService;
import com.example.librarymanagement.reader.ReaderRequest;
import com.example.librarymanagement.reader.ReaderService;
import com.example.librarymanagement.reader.ReaderStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedData(BookService bookService, ReaderService readerService) {
        return args -> {
            if (bookService.list(null, null, null).isEmpty()) {
                bookService.create(new BookRequest(
                        "软件工程导论",
                        "张海藩",
                        "9787302142180",
                        "清华大学出版社",
                        "软件工程",
                        "A-01-01",
                        LocalDate.of(2022, 1, 1),
                        5,
                        "软件工程课程设计参考书"
                ));
                bookService.create(new BookRequest(
                        "数据库系统概论",
                        "王珊",
                        "9787040406641",
                        "高等教育出版社",
                        "计算机",
                        "B-02-03",
                        LocalDate.of(2021, 8, 1),
                        3,
                        "数据库原理与应用基础教材"
                ));
            }

            if (readerService.list(null, null).isEmpty()) {
                readerService.create(new ReaderRequest(
                        "李明",
                        "男",
                        "13800000001",
                        "liming@example.com",
                        "软件工程1班",
                        "R20260001",
                        LocalDate.now(),
                        ReaderStatus.ACTIVE
                ));
                readerService.create(new ReaderRequest(
                        "王芳",
                        "女",
                        "13800000002",
                        "wangfang@example.com",
                        "软件工程2班",
                        "R20260002",
                        LocalDate.now(),
                        ReaderStatus.ACTIVE
                ));
            }
        };
    }
}
