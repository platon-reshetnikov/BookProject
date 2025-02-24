package com.epam.rd.autocode.spring.project.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDTO{
    private String clientEmail;
    private String employeeEmail;
    private LocalDateTime orderDate;
    private BigDecimal price;
    private List<BookItemDTO> bookItems;

    public OrderDTO(String clientEmail, String employeeEmail, LocalDateTime orderDate, BigDecimal price, List<BookItemDTO> bookItems) {
        this.clientEmail = clientEmail;
        this.employeeEmail = employeeEmail;
        this.orderDate = orderDate;
        this.price = price;
        this.bookItems = bookItems;
    }

    public OrderDTO(){}
}
