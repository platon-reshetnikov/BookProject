package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserWrapper {
    private ClientDTO clientDTO;
    private EmployeeDTO employeeDTO;

}