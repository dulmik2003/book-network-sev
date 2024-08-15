package com.dulmika.book.book;

import com.dulmika.book.common.PageResponse;
import com.dulmika.book.exception.OperationNotPermittedException;
import com.dulmika.book.file.FileStorageService;
import com.dulmika.book.history.BookTransactionHistory;
import com.dulmika.book.history.BookTransactionHistoryRepository;
import com.dulmika.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.dulmika.book.book.BookSpecification.withOwnerId;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository transactionHistoryRepository;
    private final FileStorageService fileStorageService;

    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = bookMapper.bookRequestToBook(request);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findBookById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::bookToBookResponse)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "No book found with the ID:" + bookId
                        )
                );
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> bookPage = bookRepository.findAllDisplayableBooks(pageable, user.getId());

        List<BookResponse> bookResponses = bookPage.stream()
                .map(bookMapper::bookToBookResponse)
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
                .map(bookMapper::bookToBookResponse)
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
                .map(bookMapper::transactionHistoryToBorrowedBookResponse)
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
                .map(bookMapper::transactionHistoryToBorrowedBookResponse)
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

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No book found with the ID:" + bookId
                ));

        User user = (User) connectedUser.getPrincipal();
        if (!Objects.equals(user.getId(), book.getOwner().getId())) {
            throw new OperationNotPermittedException("You can't update others books shareable status");
        }

        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No book found with the ID:" + bookId
                ));

        User user = (User) connectedUser.getPrincipal();
        if (!Objects.equals(user.getId(), book.getOwner().getId())) {
            throw new OperationNotPermittedException("You can't update others books archived status");
        }

        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No book found with the ID:" + bookId
                ));

        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException("You cannot borrow books since it is archived or not shareable");
        }

        User user = (User) connectedUser.getPrincipal();
        if (Objects.equals(user.getId(), book.getOwner().getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own books");
        }

        final boolean alreadyBorrowed = transactionHistoryRepository.isAlreadyBorrowed(bookId, user.getId());
        if (alreadyBorrowed) {
            throw new OperationNotPermittedException("The requested book is already borrowed");
        }

        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No book found with the ID:" + bookId
                ));

        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException("You cannot return book since it is archived or not shareable");
        }

        User user = (User) connectedUser.getPrincipal();
        if (Objects.equals(user.getId(), book.getOwner().getId())) {
            throw new OperationNotPermittedException("You cannot return your own books");
        }

        BookTransactionHistory bookTransactionHistory = transactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(
                        () -> new OperationNotPermittedException("You did not borrow this book")
                );
        bookTransactionHistory.setReturned(true);

        return  transactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer approveReturnOfBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No book found with the ID:" + bookId
                ));

        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException(
                    "You cannot approve return of a book since it is archived or not shareable"
            );
        }

        User user = (User) connectedUser.getPrincipal();
        if (Objects.equals(user.getId(), book.getOwner().getId())) {
            throw new OperationNotPermittedException("You cannot approve return of your own books");
        }

        transactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() ->
                        new OperationNotPermittedException("The book is not returned yet")
                );

        return null;
    }
}