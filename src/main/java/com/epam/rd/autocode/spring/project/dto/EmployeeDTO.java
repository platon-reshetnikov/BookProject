package com.epam.rd.autocode.spring.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeDTO{
    private String email;
    private String password;
    private String name;
    private String phone;
    private LocalDate birthDate;

    public EmployeeDTO(String email, String password, String name, String phone, LocalDate birthDate) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
    }

    public EmployeeDTO(){}
}
