package com.beyonder.bookservice.service;

import com.beyonder.bookservice.dto.BookReqDto;
import com.beyonder.bookservice.dto.BookRespDto;
import com.beyonder.bookservice.entity.Book;
import com.beyonder.bookservice.repository.BookRepository;
import com.beyonder.bookservice.repository.BookWithPaginationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookWithPaginationService {
    @Autowired
    private BookWithPaginationRepository bookWithPaginationRepository;

    public Page<Book> findBooksByAuthor(String author, int page, int size) {
        Pageable pageable = PageRequest.of(page, size); // bisa tambah Sort kalau mau
        return bookWithPaginationRepository.findBooksByAuthor(author, pageable);
    }


}
