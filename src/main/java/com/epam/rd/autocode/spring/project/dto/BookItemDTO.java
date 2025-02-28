package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.Book;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookItemDTO {
    @NotBlank(message = "Book name cannot be blank")
    private String bookName;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
