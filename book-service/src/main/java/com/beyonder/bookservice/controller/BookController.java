package com.beyonder.bookservice.controller;

import com.beyonder.bookservice.dto.BookReqDto;
import com.beyonder.bookservice.dto.BookRespDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @PostMapping("/using-dummy-dto")
    public ResponseEntity<BookRespDto> createBook(@RequestBody BookReqDto bookReqDto) {
        // Instantiate response book data (dto(data transfer object))
        BookRespDto bookRespDto = new BookRespDto();

        // Set from data request
        bookRespDto.setTitle(bookReqDto.getTitle());

        // Return to client
        return ResponseEntity.ok(bookRespDto);
    }
}
