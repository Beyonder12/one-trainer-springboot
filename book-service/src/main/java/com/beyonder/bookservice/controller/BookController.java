package com.beyonder.bookservice.controller;

import com.beyonder.bookservice.dto.BookReqDto;
import com.beyonder.bookservice.dto.BookRespDto;
import com.beyonder.bookservice.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    BookService bookService;

    @PostMapping("/using-dummy-dto")
    public ResponseEntity<BookRespDto> createBook(@RequestBody BookReqDto bookReqDto) {
        // Instantiate response book data (dto(data transfer object))
        BookRespDto bookRespDto = new BookRespDto();

        // Set from data request
        bookRespDto.setTitle(bookReqDto.getTitle());

        // Return to client
        return ResponseEntity.ok(bookRespDto);
    }

    @PostMapping("/with-db-insertion")
    public ResponseEntity<BookReqDto> createBookWithDbInsertion(@Valid @RequestBody BookReqDto bookReqDto) {
        bookService.createBookWithDbInsertion(bookReqDto);
        // Return to client
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookReqDto);
    }

    @GetMapping
    public ResponseEntity<List<BookRespDto>> getAllBooks() {
        List<BookRespDto> bookRespDtoList = bookService.getAllBooks();
        return ResponseEntity.ok(bookRespDtoList);
    }

}
