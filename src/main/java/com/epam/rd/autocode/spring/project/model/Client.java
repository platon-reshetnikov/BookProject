package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class Client extends User{
    private BigDecimal balance;


    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
