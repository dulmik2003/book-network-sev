package com.dulmika.book.common;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    // page Content
    private List<T> content;

    //requested page number
    private int number;

    //number of items per a page, that should be contained
    private int size;

    //total amount of items that contain in the whole database
    private long totalElements;

    //total amount of pages that contain in the whole database
    private int totalPages;

    //a boolean about whether the requested page is the fist page
    private boolean first;

    //a boolean about whether the requested page is the last page
    private boolean last;
}
