package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {
    Client getClientByEmail(String email);

    Employee getEmployeeByEmail(String email);

    void updateClient(String email, ClientDTO clientDTO);

    void updateEmployee(String email, EmployeeDTO employeeDTO);

    void addClient(ClientDTO clientDTO);

    void addEmployee(EmployeeDTO employeeDTO);

    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    ClientDTO findClientByEmail(String email);

    String generatePasswordResetToken(String email);

    void resetPassword(String token, String newPassword);
}
