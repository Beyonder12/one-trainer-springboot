package com.beyonder.bookservice.controller;

import com.beyonder.bookservice.entity.Book;
import com.beyonder.bookservice.service.BookWithPaginationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/book-with-pagination")
public class BookWithPaginationController {

    @Autowired
    private BookWithPaginationService bookWithPaginationService;

    @GetMapping("/author")
    public Page<Book> findBooksByAuthor(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookWithPaginationService.findBooksByAuthor(author, page, size);
    }
}
