package com.beyonder.bookservice.controller;

import com.beyonder.bookservice.dto.BookRespDto;
import com.beyonder.bookservice.dto.PageResponseDto;
import com.beyonder.bookservice.service.BookWithPaginationSortedDtoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/book-with-pagination-sorted-dto")
public class BookWithPaginationSortedDtoController {

    @Autowired
    private BookWithPaginationSortedDtoService bookWithPaginationDtoService;

    @GetMapping("/author")
    public PageResponseDto<BookRespDto> findBooksByAuthor(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookWithPaginationDtoService.findBooksByAuthorDto(author, page, size);
    }
}
