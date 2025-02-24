package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Client extends User{
    private BigDecimal balance;

    public Client(Long id, String email, String password, String name,BigDecimal balance) {
        super(id, email, password, name);
        this.balance = balance;
    }
}
