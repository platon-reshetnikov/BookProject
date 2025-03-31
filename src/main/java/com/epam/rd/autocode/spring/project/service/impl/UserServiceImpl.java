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

    public UserServiceImpl(ClientRepository clientRepository, EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, ClientService clientService) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.clientService = clientService;
    }

    @Override
    public Client getClientByEmail(String email) {
        return clientRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Client not found"));
    }

    @Override
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Override
    public void updateClient(String email, ClientDTO clientDTO) {
        logger.info("Updating client with email: {}", email);
        logger.info("ClientDTO data: {}", clientDTO);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (clientDTO.getPassword() != null && !clientDTO.getPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(clientDTO.getPassword());
            client.setPassword(hashedPassword);
        }

        client.setName(clientDTO.getName());
        client.setEmail(clientDTO.getEmail());
        client.setBalance(clientDTO.getBalance());
        clientRepository.save(client);

        logger.info("Client updated successfully: {}", client);
    }

    @Override
    public void updateEmployee(String email, EmployeeDTO employeeDTO) {
        logger.info("Updating employee with email: {}", email);
        logger.info("EmployeeDTO data: {}", employeeDTO);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(employeeDTO.getPassword());
            employee.setPassword(hashedPassword);
        }

        employee.setName(employeeDTO.getName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPhone(employeeDTO.getPhone());
        employee.setBirthDate(employeeDTO.getBirthDate());
        employeeRepository.save(employee);

        logger.info("Employee updated successfully: {}", employee);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Authenticating user with email: {}", email);
        Client client = clientRepository.findByEmail(email).orElse(null);
        if (client != null) {
            boolean isEnabled = !clientService.isClientBlocked(email); // Проверяем статус блокировки
            UserDetails userDetails = new User(client.getEmail(), client.getPassword(),
                    isEnabled, true, true, true,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));
            logger.info("Client authenticated: {}, Roles: [ROLE_CLIENT], Enabled: {}", email, isEnabled);
            return userDetails;
        }

        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        if (employee != null) {
            UserDetails userDetails = new User(employee.getEmail(), employee.getPassword(),
                    true, true, true, true,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
            logger.info("Employee authenticated: {}, Roles: [ROLE_EMPLOYEE]", email);
            return userDetails;
        }

        logger.warn("User not found with email: {}", email);
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    @Override
    public void addClient(ClientDTO clientDTO) {
        logger.info("Registering new client with email: {}", clientDTO.getEmail());

        if (clientRepository.existsByEmail(clientDTO.getEmail())) {
            logger.error("Client with email {} already exists", clientDTO.getEmail());
            throw new RuntimeException("Client with email " + clientDTO.getEmail() + " already exists");
        }

        String hashedPassword = passwordEncoder.encode(clientDTO.getPassword());

        Client client = new Client();
        client.setEmail(clientDTO.getEmail());
        client.setPassword(hashedPassword);
        client.setName(clientDTO.getName());
        client.setBalance(clientDTO.getBalance());

        clientRepository.save(client);
        logger.info("Client registered successfully: {}", client);
    }

    @Override
    public void addEmployee(EmployeeDTO employeeDTO) {
        logger.info("Registering new employee with email: {}", employeeDTO.getEmail());

        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            logger.error("Employee with email {} already exists", employeeDTO.getEmail());
            throw new RuntimeException("Employee with email " + employeeDTO.getEmail() + " already exists");
        }

        String hashedPassword = passwordEncoder.encode(employeeDTO.getPassword());

        Employee employee = new Employee();
        employee.setEmail(employeeDTO.getEmail());
        employee.setPassword(hashedPassword);
        employee.setName(employeeDTO.getName());
        employee.setPhone(employeeDTO.getPhone());
        employee.setBirthDate(employeeDTO.getBirthDate());

        employeeRepository.save(employee);
        logger.info("Employee registered successfully: {}", employee);
    }
}