package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class EmployeeRegistrationDTO {
    @Valid
    private EmployeeDTO employeeDTO;
}
