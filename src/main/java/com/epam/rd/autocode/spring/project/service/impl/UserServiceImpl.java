package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    public UserServiceImpl(ClientRepository clientRepository, EmployeeRepository employeeRepository) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
    }

    public Client getClientByEmail(String email) {
        return clientRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Client not found"));
    }

    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public void updateClient(String email, ClientDTO clientDTO) {
        logger.info("Updating client with email: {}", email);
        logger.info("ClientDTO data: {}", clientDTO);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        client.setName(clientDTO.getName());
        client.setEmail(clientDTO.getEmail());
        client.setPassword(clientDTO.getPassword());
        client.setBalance(clientDTO.getBalance());
        clientRepository.save(client);

        logger.info("Client updated successfully: {}", client);
    }

    public void updateEmployee(String email, EmployeeDTO employeeDTO) {
        logger.info("Updating employee with email: {}", email);
        logger.info("EmployeeDTO data: {}", employeeDTO);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setName(employeeDTO.getName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPassword(employeeDTO.getPassword());
        employee.setPhone(employeeDTO.getPhone());
        employee.setBirthDate(employeeDTO.getBirthDate());
        employeeRepository.save(employee);

        logger.info("Employee updated successfully: {}", employee);
    }
}
