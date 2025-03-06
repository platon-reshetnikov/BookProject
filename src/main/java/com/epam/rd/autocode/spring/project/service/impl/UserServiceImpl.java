package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ClientService clientService;


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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Authenticating user with email: {}", email);
        Client client = clientRepository.findByEmail(email).orElse(null);
        if (client != null) {
            boolean isEnabled = !clientService.isClientBlocked(email); // Проверяем статус блокировки
            UserDetails userDetails = new User(client.getEmail(), client.getPassword(),
                    true, true, true, isEnabled,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));
            logger.info("Client authenticated: {}, Roles: [ROLE_CLIENT], Enabled: {}", email, isEnabled);
            return userDetails;
        }

        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        if (employee != null) {
            UserDetails userDetails = new User(employee.getEmail(), employee.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
            logger.info("Employee authenticated: {}, Roles: [ROLE_EMPLOYEE]", email);
            return userDetails;
        }

        logger.warn("User not found with email: {}", email);
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    // Новый метод для добавления клиента
    public void addClient(ClientDTO clientDTO) {
        logger.info("Registering new client with email: {}", clientDTO.getEmail());

        // Проверка на существование клиента с таким email
        if (clientRepository.existsByEmail(clientDTO.getEmail())) {
            logger.error("Client with email {} already exists", clientDTO.getEmail());
            throw new RuntimeException("Client with email " + clientDTO.getEmail() + " already exists");
        }

        // Создаём нового клиента
        Client client = new Client();
        client.setEmail(clientDTO.getEmail());
        client.setPassword(clientDTO.getPassword());
        client.setName(clientDTO.getName());
        client.setBalance(clientDTO.getBalance());

        // Сохраняем клиента в базу данных
        clientRepository.save(client);
        logger.info("Client registered successfully: {}", client);
    }

    // Новый метод для добавления сотрудника
    public void addEmployee(EmployeeDTO employeeDTO) {
        logger.info("Registering new employee with email: {}", employeeDTO.getEmail());

        // Проверка на существование сотрудника с таким email
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            logger.error("Employee with email {} already exists", employeeDTO.getEmail());
            throw new RuntimeException("Employee with email " + employeeDTO.getEmail() + " already exists");
        }

        // Создаём нового сотрудника
        Employee employee = new Employee();
        employee.setEmail(employeeDTO.getEmail());
        employee.setPassword(employeeDTO.getPassword());
        employee.setName(employeeDTO.getName());
        employee.setPhone(employeeDTO.getPhone());
        employee.setBirthDate(employeeDTO.getBirthDate());

        // Сохраняем сотрудника в базу данных
        employeeRepository.save(employee);
        logger.info("Employee registered successfully: {}", employee);
    }
}
