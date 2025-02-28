package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@Table(name = "CLIENTS")
public class Client extends User {
    @NotNull(message = "Balance cannot be null")
    @PositiveOrZero(message = "Balance must be zero or positive")
    private BigDecimal balance;

    public Client(Long id, String email, String password, String name, BigDecimal balance) {
        super(id, email, password, name);
        this.balance = balance;
    }
}
