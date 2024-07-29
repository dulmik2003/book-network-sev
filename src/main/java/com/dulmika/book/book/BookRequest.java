package com.dulmika.book.book;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BookRequest(
        @NotNull(message = "100")
        @NotEmpty(message = "100")
        Integer id,

        @NotNull(message = "101")
        @NotEmpty(message = "101")
        String title,

        @NotNull(message = "102")
        @NotEmpty(message = "102")
        String authorName,

        @NotNull(message = "103")
        @NotEmpty(message = "103")
        String isbn,

        @NotNull(message = "104")
        @NotEmpty(message = "105")
        String synopsis,

        boolean shareable
) {

}
