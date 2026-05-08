package com.example.librarymanagement.reader;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReaderRepository extends JpaRepository<Reader, Long> {
    Optional<Reader> findByCardNumber(String cardNumber);

    boolean existsByCardNumber(String cardNumber);

    @Query("""
            select r from Reader r
            where (:keyword is null or :keyword = ''
                or lower(r.name) like lower(concat('%', :keyword, '%'))
                or lower(r.cardNumber) like lower(concat('%', :keyword, '%'))
                or lower(r.phone) like lower(concat('%', :keyword, '%')))
            and (:status is null or :status = '' or r.status = :status)
            order by r.id desc
            """)
    List<Reader> search(@Param("keyword") String keyword, @Param("status") ReaderStatus status);
}
