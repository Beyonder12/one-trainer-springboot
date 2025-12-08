package com.beyonder.bookservice.service;

import com.beyonder.bookservice.dto.BookRespDto;
import com.beyonder.bookservice.dto.PageResponseDto;
import com.beyonder.bookservice.entity.Book;
import com.beyonder.bookservice.repository.BookWithPaginationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookWithPaginationSortedDtoService {
    @Autowired
    private BookWithPaginationRepository bookWithPaginationRepository;

    public PageResponseDto<BookRespDto> findBooksByAuthorDto(String author, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("title").descending());
        Page<Book> books = bookWithPaginationRepository.findBooksByAuthor(author, pageable);

        List<BookRespDto> dtoList = books.getContent().stream()
                .map(b -> new BookRespDto(
                        b.getId(),
                        b.getTitle(),
                        b.getAuthor()
                ))
                .toList();

        return new PageResponseDto<>(
                dtoList,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isLast()
        );
    }


}
