package com.dulmika.book.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Integer> {
    @Query(
            """
            SELECT book
            FROM Book book
            WHERE book.archive = false
            AND book.sharable = true
            AND book.ownerId != :userId
            """
    )
    Page<Book> findAllDisplayableBooks(Pageable pageable, Integer userId);
}
