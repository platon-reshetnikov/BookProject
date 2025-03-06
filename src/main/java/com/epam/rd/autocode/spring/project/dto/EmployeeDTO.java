package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import validation.EmployeeValidationGroup;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeDTO {
    @NotBlank(message = "Email cannot be blank", groups = EmployeeValidationGroup.class)
    @Email(message = "Email must be a valid email address", groups = EmployeeValidationGroup.class)
    private String email;

    @NotBlank(message = "Password cannot be blank", groups = EmployeeValidationGroup.class)
    @Size(min = 6, message = "Password must be at least 6 characters long", groups = EmployeeValidationGroup.class)
    private String password;

    @NotBlank(message = "Name cannot be blank", groups = EmployeeValidationGroup.class)
    private String name;

    @NotBlank(message = "Phone number cannot be blank", groups = EmployeeValidationGroup.class)
    @Pattern(regexp = "\\d{3}-\\d{3}-\\d{4}", message = "Phone must be in format XXX-XXX-XXXX", groups = EmployeeValidationGroup.class)
    private String phone;

    @NotNull(message = "Birth date cannot be null", groups = EmployeeValidationGroup.class)
    @Past(message = "Birth date must be in the past", groups = EmployeeValidationGroup.class)
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Ensure correct formatting
    private LocalDate birthDate;
}
