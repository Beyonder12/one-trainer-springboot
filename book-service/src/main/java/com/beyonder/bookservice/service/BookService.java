package com.beyonder.bookservice.service;

import com.beyonder.bookservice.dto.BookReqDto;
import com.beyonder.bookservice.dto.BookRespDto;
import com.beyonder.bookservice.entity.Book;
import com.beyonder.bookservice.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    @Autowired
    BookRepository bookRepository;

    @Transactional
    public void createBookWithDbInsertion(BookReqDto bookReqDto) {
        Book book = new Book();
        book.setAuthor(bookReqDto.getAuthor());
        book.setTitle(bookReqDto.getTitle());

        bookRepository.save(book);
    }

    public List<BookRespDto> getAllBooks() {
        List<BookRespDto> bookRespDtoList = new ArrayList<>();
        List<Book> bookList = bookRepository.findAll();
        for (Book book : bookList) {
            BookRespDto bookRespDto = new BookRespDto();
            bookRespDto.setId(book.getId());
            bookRespDto.setAuthor(book.getAuthor());
            bookRespDto.setTitle(book.getTitle());
            bookRespDtoList.add(bookRespDto);
        }
        return bookRespDtoList;
    }

    public Page<Book> getAllBooksPageable(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

}
