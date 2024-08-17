package com.dulmika.book.feedback;

import com.dulmika.book.book.Book;
import org.springframework.stereotype.Service;

@Service
public class FeedbackMapper {
    public Feedback feedbackRequestToFeedback(FeedbackRequest request) {
        return Feedback.builder()
                .note(request.note())
                .comment(request.comment())
                .book(Book.builder()
                        .id(request.bookId())
                        .shareable(false) // Not required and has no impact :: just to satisfy lombok
                        .archived(false) // Not required and has no impact :: just to satisfy lombok
                        .build()
                )
                .build();
    }
}
