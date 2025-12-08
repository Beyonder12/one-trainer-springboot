package com.beyonder.bookservice.repository;

import com.beyonder.bookservice.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookWithPaginationRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b from Book b where b.author = :authorName")
    Page<Book> findBooksByAuthor(@Param("authorName") String authorName, Pageable pageable);
}
