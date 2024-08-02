package com.dulmika.book.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookTransactionHistoryRepository extends JpaRepository<BookTransactionHistory, Integer> {
    @Query(
            """
            SELECT history
            FROM BookTransactionHistory history
            WHERE history.user.id = :userId
            """
    )
    Page<BookTransactionHistory> findAllBorrowedBooks(Pageable pageable, Integer userId);

    @Query(
            """
            SELECT history
            FROM BookTransactionHistory history
            WHERE history.book.ownerId = :userId
            AND history.returned = true
            """
    )
    Page<BookTransactionHistory> findAllReturnedBooks(Pageable pageable, Integer id);

    @Query(
            """
            SELECT (count(*) > 0 ) AS isBorrowed
            FROM BookTransactionHistory history
            WHERE history.userId = :userId
            AND history.book.id = :bookId
            AND history.returnApproved = false
            """
    )
    boolean isAlreadyBorrowed(Integer bookId, Integer userId);
}
