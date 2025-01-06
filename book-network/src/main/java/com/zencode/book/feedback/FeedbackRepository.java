package com.zencode.book.feedback;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @Query("""
            SELECT f
            FROM Feedback f
            WHERE f.book.id = :bookId
            """)
    Page<Feedback> findAllByBookId(Long bookId, Pageable pageable);
}
