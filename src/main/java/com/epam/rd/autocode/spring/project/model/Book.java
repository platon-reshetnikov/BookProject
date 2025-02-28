package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "BOOKS")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Genre cannot be blank")
    private String genre;

    @NotNull(message = "Age group cannot be null")
    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Publication year cannot be null")
    @PastOrPresent(message = "Publication year must be in the past or present")
    @Column(name = "publication_year")
    private LocalDate publicationDate;

    @NotBlank(message = "Author cannot be blank")
    private String author;

    @NotNull(message = "Number of pages cannot be null")
    @Positive(message = "Number of pages must be positive")
    @Column(name = "number_of_pages")
    private Integer pages;

    @NotBlank(message = "Characteristics cannot be blank")
    private String characteristics;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Language cannot be null")
    @Enumerated(EnumType.STRING)
    private Language language;
}
