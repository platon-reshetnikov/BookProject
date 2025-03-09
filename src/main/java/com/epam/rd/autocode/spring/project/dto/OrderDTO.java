package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    @NotBlank(message = "Client email cannot be blank")
    @Email(message = "Client email must be a valid email address")
    private String clientEmail;

    @NotBlank(message = "Employee email cannot be blank")
    @Email(message = "Employee email must be a valid email address")
    private String employeeEmail;

    @NotNull(message = "Order date cannot be null")
    @PastOrPresent(message = "Order date must be in the past or present")
    private LocalDateTime orderDate;

    @NotNull(message = "Price cannot be null")
    @PositiveOrZero(message = "Price must be zero or positive")
    private BigDecimal price;

    @NotNull(message = "Book items list cannot be null")
    @Size(min = 1, message = "Order must contain at least one book item")
    private List<@Valid BookItemDTO> bookItems;
}
