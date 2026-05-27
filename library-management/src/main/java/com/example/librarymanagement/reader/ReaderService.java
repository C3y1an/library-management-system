package com.example.librarymanagement.reader;

import com.example.librarymanagement.borrow.BorrowRecordRepository;
import com.example.librarymanagement.borrow.BorrowStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReaderService {
    private final ReaderRepository readerRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public ReaderService(ReaderRepository readerRepository, BorrowRecordRepository borrowRecordRepository) {
        this.readerRepository = readerRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    public List<Reader> list(String keyword, ReaderStatus status) {
        return readerRepository.search(keyword, status);
    }

    public Reader get(Long id) {
        return readerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("读者不存在"));
    }

    @Transactional
    public Reader create(ReaderRequest request) {
        if (readerRepository.existsByCardNumber(request.cardNumber())) {
            throw new IllegalArgumentException("借阅证号已存在");
        }
        Reader reader = new Reader();
        applyRequest(reader, request);
        return readerRepository.save(reader);
    }

    @Transactional
    public Reader update(Long id, ReaderRequest request) {
        Reader reader = get(id);
        readerRepository.findByCardNumber(request.cardNumber())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("借阅证号已存在");
                });
        applyRequest(reader, request);
        return readerRepository.save(reader);
    }

    @Transactional
    public void delete(Long id) {
        Reader reader = get(id);
        boolean hasBorrowedBooks = borrowRecordRepository.existsByReaderIdAndStatus(id, BorrowStatus.BORROWED);
        if (hasBorrowedBooks) {
            throw new IllegalArgumentException("该读者仍有未归还图书，不能删除");
        }
        if (borrowRecordRepository.existsByReaderId(id)) {
            throw new IllegalArgumentException("该读者已有借阅历史，不能直接删除");
        }
        readerRepository.delete(reader);
    }

    private void applyRequest(Reader reader, ReaderRequest request) {
        reader.setName(request.name());
        reader.setGender(request.gender());
        reader.setPhone(request.phone());
        reader.setEmail(request.email());
        reader.setDepartment(request.department());
        reader.setCardNumber(request.cardNumber());
        reader.setRegisteredDate(request.registeredDate());
        reader.setStatus(request.status() == null ? ReaderStatus.ACTIVE : request.status());
    }
}
