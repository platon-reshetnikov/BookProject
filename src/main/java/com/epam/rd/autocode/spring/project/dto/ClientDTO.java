package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import validation.ClientValidationGroup;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDTO {
    @NotBlank(message = "Email cannot be blank", groups = ClientValidationGroup.class)
    @Email(message = "Email must be a valid email address", groups = ClientValidationGroup.class)
    private String email;

    @NotBlank(message = "Password cannot be blank", groups = ClientValidationGroup.class)
    @Size(min = 6, message = "Password must be at least 6 characters long", groups = ClientValidationGroup.class)
    private String password;

    @NotBlank(message = "Name cannot be blank", groups = ClientValidationGroup.class)
    private String name;

    @NotNull(message = "Balance cannot be null", groups = ClientValidationGroup.class)
    @PositiveOrZero(message = "Balance must be zero or positive", groups = ClientValidationGroup.class)
    private BigDecimal balance;
}