package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class BookItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Book cannot be null")
    @ManyToOne
    private Book book;

    @NotNull(message = "Order cannot be null")
    @ManyToOne
    private Order order;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
