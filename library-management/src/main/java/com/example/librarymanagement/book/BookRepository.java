package com.example.librarymanagement.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    @Query("""
            select b from Book b
            where (:keyword is null or :keyword = ''
                or lower(b.title) like lower(concat('%', :keyword, '%'))
                or lower(b.author) like lower(concat('%', :keyword, '%'))
                or lower(b.isbn) like lower(concat('%', :keyword, '%')))
            and (:category is null or :category = '' or b.category = :category)
            order by b.id desc
            """)
    List<Book> search(@Param("keyword") String keyword, @Param("category") String category);
}
