package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserWrapper {
    private ClientDTO clientDTO;
    private EmployeeDTO employeeDTO;

    public UserWrapper() {
        this.clientDTO = new ClientDTO();
        this.employeeDTO = new EmployeeDTO();
    }

    public UserWrapper(ClientDTO clientDTO, EmployeeDTO employeeDTO) {
        this.clientDTO = clientDTO != null ? clientDTO : new ClientDTO();
        this.employeeDTO = employeeDTO != null ? employeeDTO : new EmployeeDTO();
    }
}