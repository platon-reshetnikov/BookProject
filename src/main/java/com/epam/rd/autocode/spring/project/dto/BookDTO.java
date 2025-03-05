package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Genre cannot be blank")
    private String genre;

    @NotNull(message = "Age group cannot be null")
    private AgeGroup ageGroup;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Publication date cannot be null")
    @PastOrPresent(message = "Publication date must be in the past or present")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;

    @NotBlank(message = "Author cannot be blank")
    private String author;

    @NotNull(message = "Pages cannot be null")
    @Positive(message = "Pages must be positive")
    private Integer pages;

    @NotBlank(message = "Characteristics cannot be blank")
    private String characteristics;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Language cannot be null")
    private Language language;
}
