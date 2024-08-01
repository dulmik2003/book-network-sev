package com.dulmika.book.book;

import com.dulmika.book.common.PageResponse;
import com.dulmika.book.history.BookTransactionHistory;
import com.dulmika.book.history.BookTransactionHistoryRepository;
import com.dulmika.book.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dulmika.book.book.BookSpecification.withOwnerId;

@Service
@RequiredArgsConstructor
public class BookService {
    private final ObjectMapper mapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository transactionHistoryRepository;

    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = mapper.convertValue(request, Book.class);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findBookById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(book ->
                        mapper.convertValue(book, BookResponse.class)
                )
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "No book found with the ID::" + bookId
                        )
                );
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> bookPage = bookRepository.findAllDisplayableBooks(pageable, user.getId());

        List<BookResponse> bookResponses = bookPage.stream()
                .map(book ->
                        mapper.convertValue(book, BookResponse.class)
                )
                .toList();

        return new PageResponse<>(
                bookResponses,
                bookPage.getNumber(),
                bookPage.getSize(),
                bookPage.getTotalElements(),
                bookPage.getTotalPages(),
                bookPage.isFirst(),
                bookPage.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> bookPage = bookRepository.findAll(withOwnerId(user.getId()), pageable);

        List<BookResponse> bookResponses = bookPage.stream()
                .map(book ->
                        mapper.convertValue(book, BookResponse.class)
                )
                .toList();

        return new PageResponse<>(
                bookResponses,
                page,
                size,
                bookPage.getTotalElements(),
                bookPage.getTotalPages(),
                bookPage.isFirst(),
                bookPage.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> borrowedBooksPage = transactionHistoryRepository.findAllBorrowedBooks(
                pageable, user.getId()
        );

        List<BorrowedBookResponse> borrowedBookResponses = borrowedBooksPage.stream()
                .map(history -> {
                    BorrowedBookResponse borrowedBookResponse = mapper.convertValue(
                            history.getBook(),
                            BorrowedBookResponse.class
                    );
                    borrowedBookResponse.setReturned(history.isReturned());
                    borrowedBookResponse.setReturnApproved(history.isReturnApproved());
                    return borrowedBookResponse;
                })
                .toList();

        return new PageResponse<>(
                borrowedBookResponses,
                page,
                size,
                borrowedBooksPage.getTotalElements(),
                borrowedBooksPage.getTotalPages(),
                borrowedBooksPage.isFirst(),
                borrowedBooksPage.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> borrowedBooksPage = transactionHistoryRepository.findAllReturnedBooks(
                pageable, user.getId()
        );

        List<BorrowedBookResponse> borrowedBookResponses = borrowedBooksPage.stream()
                .map(history -> {
                    BorrowedBookResponse borrowedBookResponse = mapper.convertValue(
                            history.getBook(),
                            BorrowedBookResponse.class
                    );
                    borrowedBookResponse.setReturned(history.isReturned());
                    borrowedBookResponse.setReturnApproved(history.isReturnApproved());
                    return borrowedBookResponse;
                })
                .toList();

        return new PageResponse<>(
                borrowedBookResponses,
                page,
                size,
                borrowedBooksPage.getTotalElements(),
                borrowedBooksPage.getTotalPages(),
                borrowedBooksPage.isFirst(),
                borrowedBooksPage.isLast()
        );
    }
}