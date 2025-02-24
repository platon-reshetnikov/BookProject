package com.epam.rd.autocode.spring.project.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO{
    private String email;
    private String password;
    private String name;
    private String phone;
    private LocalDate birthDate;
}
