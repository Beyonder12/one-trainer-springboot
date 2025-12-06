package com.beyonder.bookservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String author;

    private LocalDate publishDate;

    private Integer pages;

    public Book() {}

    public Book(Long id, String title, String author, LocalDate publishDate, Integer pages) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publishDate = publishDate;
        this.pages = pages;
    }

    // getter & setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }

    public Integer getPages() { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }
}
