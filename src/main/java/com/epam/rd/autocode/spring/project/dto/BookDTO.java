package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class BookDTO{
    private String name;
    private String genre;
    private AgeGroup ageGroup;
    private BigDecimal price;
    private LocalDate publicationDate;
    private String author;
    private Integer pages;
    private String characteristics;
    private String description;
    private Language language;

    public BookDTO(String name, String genre, AgeGroup ageGroup, BigDecimal price, LocalDate publicationDate, String author, Integer pages, String characteristics, String description, Language language) {
        this.name = name;
        this.genre = genre;
        this.ageGroup = ageGroup;
        this.price = price;
        this.publicationDate = publicationDate;
        this.author = author;
        this.pages = pages;
        this.characteristics = characteristics;
        this.description = description;
        this.language = language;
    }

    public BookDTO(){}
}
