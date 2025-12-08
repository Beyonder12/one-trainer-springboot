package com.beyonder.bookservice.controller;

import com.beyonder.bookservice.entity.Book;
import com.beyonder.bookservice.service.BookManualQueryService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books-manual-query")
public class BookManualQueryController {

    @Autowired
    private BookManualQueryService bookManualQueryService;

    // JPQL
    @GetMapping("/title-containing/{title}")
    public List<Book> findBooksByTitleContaining(@PathVariable("title") String title) {
        return bookManualQueryService.findBooksByTitleContaining(title);
    }

    @GetMapping("/author")
    public List<Book> findBooksByAuthor(@PathParam("author") String author) {
        return bookManualQueryService.findBooksByAuthor(author);
    }

    // Native SQL
    @GetMapping("/title")
    public List<Book> findBooksByTitleNative(@PathParam("title") String title) {
        return bookManualQueryService.findBooksByTitleNative(title);
    }
}
