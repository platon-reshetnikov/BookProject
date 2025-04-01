package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.UserService;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Primary
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    @Lazy
    private final ClientService clientService;

    public UserServiceImpl(ClientRepository clientRepository, EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder, ClientService clientService) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.clientService = clientService;
    }

    @Override
    public Client getClientByEmail(String email) {
        logger.info("Retrieving client by email: {}", email);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Client not found with email: {}", email);
                    return new RuntimeException("Client not found");
                });

        logger.debug("Client retrieved: {}", client);
        return client;
    }

    @Override
    public Employee getEmployeeByEmail(String email) {
        logger.info("Retrieving employee by email: {}", email);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Employee not found with email: {}", email);
                    return new RuntimeException("Employee not found");
                });

        logger.debug("Employee retrieved: {}", employee);
        return employee;
    }

    @Override
    public void updateClient(String email, ClientDTO clientDTO) {
        logger.info("Updating client - Email: {}, ClientDTO: {}", email, clientDTO);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Client not found for update with email: {}", email);
                    return new RuntimeException("Client not found");
                });

        logger.debug("Existing client before update: {}", client);

        if (clientDTO.getPassword() != null && !clientDTO.getPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(clientDTO.getPassword());
            client.setPassword(hashedPassword);
            logger.debug("Updated client password (hashed): {}", hashedPassword);
        }

        client.setName(clientDTO.getName());
        client.setEmail(clientDTO.getEmail());
        client.setBalance(clientDTO.getBalance());

        Client updatedClient = clientRepository.save(client);
        logger.info("Client updated successfully: {}", updatedClient.getEmail());
        logger.debug("Updated client details: {}", updatedClient);
    }

    @Override
    public void updateEmployee(String email, EmployeeDTO employeeDTO) {
        logger.info("Updating employee - Email: {}, EmployeeDTO: {}", email, employeeDTO);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Employee not found for update with email: {}", email);
                    return new RuntimeException("Employee not found");
                });

        logger.debug("Existing employee before update: {}", employee);

        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(employeeDTO.getPassword());
            employee.setPassword(hashedPassword);
            logger.debug("Updated employee password (hashed): {}", hashedPassword);
        }

        employee.setName(employeeDTO.getName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPhone(employeeDTO.getPhone());
        employee.setBirthDate(employeeDTO.getBirthDate());

        Employee updatedEmployee = employeeRepository.save(employee);
        logger.info("Employee updated successfully: {}", updatedEmployee.getEmail());
        logger.debug("Updated employee details: {}", updatedEmployee);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Authenticating user - Email: {}", email);

        Client client = clientRepository.findByEmail(email).orElse(null);
        if (client != null) {
            boolean isEnabled = !clientService.isClientBlocked(email);
            UserDetails userDetails = new User(client.getEmail(), client.getPassword(),
                    isEnabled, true, true, true,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));
            logger.info("Client authenticated - Email: {}, Enabled: {}, Roles: [ROLE_CLIENT]", email, isEnabled);
            logger.debug("Client details: {}", client);
            return userDetails;
        }

        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        if (employee != null) {
            UserDetails userDetails = new User(employee.getEmail(), employee.getPassword(),
                    true, true, true, true,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
            logger.info("Employee authenticated - Email: {}, Roles: [ROLE_EMPLOYEE]", email);
            logger.debug("Employee details: {}", employee);
            return userDetails;
        }

        logger.warn("Authentication failed - User not found with email: {}", email);
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    @Override
    public void addClient(ClientDTO clientDTO) {
        logger.info("Registering new client - ClientDTO: {}", clientDTO);

        if (clientRepository.existsByEmail(clientDTO.getEmail())) {
            logger.warn("Client registration failed - Email already exists: {}", clientDTO.getEmail());
            throw new RuntimeException("Client with email " + clientDTO.getEmail() + " already exists");
        }

        String hashedPassword = passwordEncoder.encode(clientDTO.getPassword());
        Client client = new Client();
        client.setEmail(clientDTO.getEmail());
        client.setPassword(hashedPassword);
        client.setName(clientDTO.getName());
        client.setBalance(clientDTO.getBalance());

        Client savedClient = clientRepository.save(client);
        logger.info("Client registered successfully: {}", savedClient.getEmail());
        logger.debug("Saved client details: {}", savedClient);
    }

    @Override
    public void addEmployee(EmployeeDTO employeeDTO) {
        logger.info("Registering new employee - EmployeeDTO: {}", employeeDTO);

        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            logger.warn("Employee registration failed - Email already exists: {}", employeeDTO.getEmail());
            throw new RuntimeException("Employee with email " + employeeDTO.getEmail() + " already exists");
        }

        String hashedPassword = passwordEncoder.encode(employeeDTO.getPassword());
        Employee employee = new Employee();
        employee.setEmail(employeeDTO.getEmail());
        employee.setPassword(hashedPassword);
        employee.setName(employeeDTO.getName());
        employee.setPhone(employeeDTO.getPhone());
        employee.setBirthDate(employeeDTO.getBirthDate());

        Employee savedEmployee = employeeRepository.save(employee);
        logger.info("Employee registered successfully: {}", savedEmployee.getEmail());
        logger.debug("Saved employee details: {}", savedEmployee);
    }
}