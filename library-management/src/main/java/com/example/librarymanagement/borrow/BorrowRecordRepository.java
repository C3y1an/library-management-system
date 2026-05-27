package com.example.librarymanagement.borrow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    long countByStatus(BorrowStatus status);

    boolean existsByBookId(Long bookId);

    boolean existsByReaderId(Long readerId);

    boolean existsByReaderIdAndStatus(Long readerId, BorrowStatus status);

    @Query("""
            select br from BorrowRecord br
            join br.book b
            join br.reader r
            where (:keyword is null or :keyword = ''
                or lower(b.title) like lower(concat('%', :keyword, '%'))
                or lower(b.isbn) like lower(concat('%', :keyword, '%'))
                or lower(r.name) like lower(concat('%', :keyword, '%'))
                or lower(r.cardNumber) like lower(concat('%', :keyword, '%')))
            and (:status is null or br.status = :status)
            order by br.id desc
            """)
    List<BorrowRecord> search(@Param("keyword") String keyword, @Param("status") BorrowStatus status);
}
