package com.beyonder.bookservice.controller;

import com.beyonder.bookservice.dto.BookReqDto;
import com.beyonder.bookservice.dto.BookRespDto;
import com.beyonder.bookservice.entity.Book;
import com.beyonder.bookservice.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books-manual-query")
public class BookManualQueryController {

    @Autowired
    BookService bookService;

    @GetMapping("/title-containing/{title}")
    public List<Book> findBooksByTitleContaining(@PathVariable("title") String title) {
        return bookService.findBooksByTitleContaining(title);
    }
}
