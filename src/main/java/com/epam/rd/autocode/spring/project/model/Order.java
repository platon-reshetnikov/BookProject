package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Client client;
    @ManyToOne
    private Employee employee;

    private LocalDateTime orderDate;
    private BigDecimal price;

    @OneToMany(mappedBy = "order")
    private List<BookItem> bookItems;

    public Order(Long id, Client client, Employee employee, LocalDateTime orderDate, BigDecimal price, List<BookItem> bookItems) {
        this.id = id;
        this.client = client;
        this.employee = employee;
        this.orderDate = orderDate;
        this.price = price;
        this.bookItems = bookItems;
    }

    public Order() {

    }
}
