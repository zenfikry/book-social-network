package com.zencode.book.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookTransactionHistoryRepository extends JpaRepository<BookTransactionHistory, Long> {
    @Query("""
            SELECT h 
            FROM BookTransactionHistory h 
            WHERE h.user.id = :userId
            """)
    Page<BookTransactionHistory> findAllBorrowedBooks(Pageable pageable, Long userId);

    @Query("""
            SELECT h 
            FROM BookTransactionHistory h 
            WHERE h.book.owner.id = :userId
            """)
    Page<BookTransactionHistory> findAllReturnedBooks(Pageable pageable, Long userId);

    @Query("""
            SELECT (COUNT(*) > 0) AS isBorrowed 
            FROM BookTransactionHistory h 
            WHERE h.user.id = :userId AND h.book.id = :bookId AND h.returnApproved = false
            """)
    boolean isAlreadyBorrowed(Long bookId, Long userId);

    @Query("""
            SELECT h 
            FROM BookTransactionHistory h 
            WHERE h.user.id = :userId AND h.book.id = :bookId AND h.returned = false AND h.returnApproved = false
            """)
    Optional<BookTransactionHistory> findByBookIdAndUserId(Long bookId, Long userId);
}
