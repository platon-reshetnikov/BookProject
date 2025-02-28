package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDTO {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Balance cannot be null")
    @PositiveOrZero(message = "Balance must be zero or positive")
    private BigDecimal balance;
}
