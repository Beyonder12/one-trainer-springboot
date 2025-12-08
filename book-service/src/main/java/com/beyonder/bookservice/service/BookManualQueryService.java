package com.beyonder.bookservice.service;

import com.beyonder.bookservice.entity.Book;
import com.beyonder.bookservice.repository.BookManualQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookManualQueryService {

    @Autowired
    BookManualQueryRepository bookManualQueryRepository;

    public List<Book> findBooksByTitleContaining(String title) {
        return bookManualQueryRepository.findBooksByTitleContaining(title);
    }

    public List<Book> findBooksByAuthor(String author) {
        return bookManualQueryRepository.findBooksByAuthor(author);
    }

    public List<Book> findBooksByTitleNative(String title) {
        return bookManualQueryRepository.findBooksByTitleNative(title);
    }
}
