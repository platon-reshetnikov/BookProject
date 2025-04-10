package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.UserService;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Primary
public class UserServiceImpl implements UserService {
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    @Lazy
    private final ClientService clientService;
    private final JavaMailSender mailSender;
    private final ClientMapper clientMapper;

    private final Map<String, String> resetTokens = new ConcurrentHashMap<>();

    public UserServiceImpl(ClientRepository clientRepository, EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder, ClientService clientService, JavaMailSender mailSender, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.clientService = clientService;
        this.mailSender = mailSender;
        this.clientMapper = clientMapper;
    }

    @Override
    public Client getClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    @Override
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Override
    public void updateClient(String email, ClientDTO clientDTO) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        if (clientDTO.getPassword() != null && !clientDTO.getPassword().isEmpty()) {
            client.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        }
        client.setName(clientDTO.getName());
        client.setEmail(clientDTO.getEmail());
        client.setBalance(clientDTO.getBalance());
        clientRepository.save(client);
    }

    @Override
    public void updateEmployee(String email, EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        }
        employee.setName(employeeDTO.getName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPhone(employeeDTO.getPhone());
        employee.setBirthDate(employeeDTO.getBirthDate());
        employeeRepository.save(employee);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Client client = clientRepository.findByEmail(email).orElse(null);
        if (client != null) {
            boolean isEnabled = !clientService.isClientBlocked(email);
            return new User(client.getEmail(), client.getPassword(),
                    isEnabled, true, true, true,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));
        }
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        if (employee != null) {
            return new User(employee.getEmail(), employee.getPassword(),
                    true, true, true, true,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        }
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    @Override
    public void addClient(ClientDTO clientDTO) {
        if (clientRepository.existsByEmail(clientDTO.getEmail())) {
            throw new RuntimeException("Client with email " + clientDTO.getEmail() + " already exists");
        }
        Client client = new Client();
        client.setEmail(clientDTO.getEmail());
        client.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        client.setName(clientDTO.getName());
        client.setBalance(clientDTO.getBalance());
        clientRepository.save(client);
    }

    @Override
    public void addEmployee(EmployeeDTO employeeDTO) {
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new RuntimeException("Employee with email " + employeeDTO.getEmail() + " already exists");
        }
        Employee employee = new Employee();
        employee.setEmail(employeeDTO.getEmail());
        employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        employee.setName(employeeDTO.getName());
        employee.setPhone(employeeDTO.getPhone());
        employee.setBirthDate(employeeDTO.getBirthDate());
        employeeRepository.save(employee);
    }

    @Override
    public ClientDTO findClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        return clientMapper.toDTO(client);
    }

    @Override
    public String generatePasswordResetToken(String email) {
        ClientDTO user = findClientByEmail(email);
        String token = UUID.randomUUID().toString();
        resetTokens.put(token, email);

        String resetLink = "https://localhost:8443/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ctrather2@gmail.com");
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" + resetLink);
        mailSender.send(message);
        return token;
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        String email = resetTokens.get(token);
        if (email == null) {
            throw new NotFoundException("Invalid or expired token");
        }

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        client.setPassword(passwordEncoder.encode(newPassword));
        try {
            clientRepository.save(client);
            resetTokens.remove(token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset password", e);
        }
    }
}