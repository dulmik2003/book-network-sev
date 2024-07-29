package com.dulmika.book.book;

import com.dulmika.book.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {
    private final ObjectMapper mapper;
    private final BookRepository bookRepository;

    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = mapper.convertValue(request, Book.class);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }
}
