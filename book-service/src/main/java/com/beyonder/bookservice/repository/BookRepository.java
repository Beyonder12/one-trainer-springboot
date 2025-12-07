package com.beyonder.bookservice.repository;

import com.beyonder.bookservice.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // JPQL (Java Persistence Query Language)
    @Query("SELECT b FROM Book b WHERE b.title LIKE %?1%")
    List<Book> findBooksByTitleContaining(String title);

    @Query("SELECT b FROM Book b WHERE b.author = :authorName")
    List<Book> findBooksByAuthor(@Param("authorName") String authorName);

}
