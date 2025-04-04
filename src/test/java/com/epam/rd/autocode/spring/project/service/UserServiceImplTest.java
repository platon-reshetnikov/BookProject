package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ClientService clientService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;
    private Client client;
    private Employee employee;
    private ClientDTO clientDTO;
    private EmployeeDTO employeeDTO;
    private static final String CLIENT_EMAIL = "client@example.com";
    private static final String EMPLOYEE_EMAIL = "employee@example.com";
    private static final String PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "hashedPassword123";

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setEmail(CLIENT_EMAIL);
        client.setPassword(HASHED_PASSWORD);
        client.setName("Client Name");
        client.setBalance(BigDecimal.valueOf(100));

        employee = new Employee();
        employee.setEmail(EMPLOYEE_EMAIL);
        employee.setPassword(HASHED_PASSWORD);
        employee.setName("Employee Name");
        employee.setPhone("1234567890");
        employee.setBirthDate(LocalDate.of(1990, 1, 1));

        clientDTO = new ClientDTO();
        clientDTO.setEmail(CLIENT_EMAIL);
        clientDTO.setPassword(PASSWORD);
        clientDTO.setName("Updated Client Name");
        clientDTO.setBalance(BigDecimal.valueOf(200));

        employeeDTO = new EmployeeDTO();
        employeeDTO.setEmail(EMPLOYEE_EMAIL);
        employeeDTO.setPassword(PASSWORD);
        employeeDTO.setName("Updated Employee Name");
        employeeDTO.setPhone("0987654321");
        employeeDTO.setBirthDate(LocalDate.of(1995, 5, 5));
    }

    @Test
    void getClientByEmail_ClientExists_ReturnsClient() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));

        Client result = userService.getClientByEmail(CLIENT_EMAIL);

        assertEquals(client, result);
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void getClientByEmail_ClientNotFound_ThrowsRuntimeException() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getClientByEmail(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }


    @Test
    void getEmployeeByEmail_EmployeeExists_ReturnsEmployee() {
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(employee));

        Employee result = userService.getEmployeeByEmail(EMPLOYEE_EMAIL);

        assertEquals(employee, result);
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
    }

    @Test
    void getEmployeeByEmail_EmployeeNotFound_ThrowsRuntimeException() {
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getEmployeeByEmail(EMPLOYEE_EMAIL));
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
    }

    @Test
    void updateClient_ClientExistsWithPassword_UpdatesClient() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        userService.updateClient(CLIENT_EMAIL, clientDTO);

        assertEquals(clientDTO.getName(), client.getName());
        assertEquals(clientDTO.getEmail(), client.getEmail());
        assertEquals(HASHED_PASSWORD, client.getPassword());
        assertEquals(clientDTO.getBalance(), client.getBalance());
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(passwordEncoder, times(1)).encode(PASSWORD);
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void updateClient_ClientExistsNoPassword_UpdatesClientWithoutPasswordChange() {
        clientDTO.setPassword(null);
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        userService.updateClient(CLIENT_EMAIL, clientDTO);

        assertEquals(clientDTO.getName(), client.getName());
        assertEquals(clientDTO.getEmail(), client.getEmail());
        assertEquals(HASHED_PASSWORD, client.getPassword()); // Пароль не изменился
        assertEquals(clientDTO.getBalance(), client.getBalance());
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(passwordEncoder, never()).encode(anyString());
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void updateClient_ClientNotFound_ThrowsRuntimeException() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateClient(CLIENT_EMAIL, clientDTO));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void updateEmployee_EmployeeExistsWithPassword_UpdatesEmployee() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        userService.updateEmployee(EMPLOYEE_EMAIL, employeeDTO);

        assertEquals(employeeDTO.getName(), employee.getName());
        assertEquals(employeeDTO.getEmail(), employee.getEmail());
        assertEquals(HASHED_PASSWORD, employee.getPassword());
        assertEquals(employeeDTO.getPhone(), employee.getPhone());
        assertEquals(employeeDTO.getBirthDate(), employee.getBirthDate());
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
        verify(passwordEncoder, times(1)).encode(PASSWORD);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void updateEmployee_EmployeeExistsNoPassword_UpdatesEmployeeWithoutPasswordChange() {
        employeeDTO.setPassword(null);
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        userService.updateEmployee(EMPLOYEE_EMAIL, employeeDTO);

        assertEquals(employeeDTO.getName(), employee.getName());
        assertEquals(employeeDTO.getEmail(), employee.getEmail());
        assertEquals(HASHED_PASSWORD, employee.getPassword()); // Пароль не изменился
        assertEquals(employeeDTO.getPhone(), employee.getPhone());
        assertEquals(employeeDTO.getBirthDate(), employee.getBirthDate());
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
        verify(passwordEncoder, never()).encode(anyString());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void updateEmployee_EmployeeNotFound_ThrowsRuntimeException() {
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateEmployee(EMPLOYEE_EMAIL, employeeDTO));
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void loadUserByUsername_ClientExistsNotBlocked_ReturnsUserDetails() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(clientService.isClientBlocked(CLIENT_EMAIL)).thenReturn(false);


        UserDetails result = userService.loadUserByUsername(CLIENT_EMAIL);


        assertEquals(CLIENT_EMAIL, result.getUsername());
        assertEquals(HASHED_PASSWORD, result.getPassword());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT")));
        assertTrue(result.isEnabled());
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(clientService, times(1)).isClientBlocked(CLIENT_EMAIL);
        verify(employeeRepository, never()).findByEmail(anyString());
    }

    @Test
    void loadUserByUsername_ClientExistsBlocked_ReturnsUserDetailsDisabled() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(clientService.isClientBlocked(CLIENT_EMAIL)).thenReturn(true);


        UserDetails result = userService.loadUserByUsername(CLIENT_EMAIL);

        assertEquals(CLIENT_EMAIL, result.getUsername());
        assertEquals(HASHED_PASSWORD, result.getPassword());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT")));
        assertFalse(result.isEnabled());
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(clientService, times(1)).isClientBlocked(CLIENT_EMAIL);
    }

    @Test
    void loadUserByUsername_EmployeeExists_ReturnsUserDetails() {
        when(clientRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(employee));

        UserDetails result = userService.loadUserByUsername(EMPLOYEE_EMAIL);

        assertEquals(EMPLOYEE_EMAIL, result.getUsername());
        assertEquals(HASHED_PASSWORD, result.getPassword());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        assertTrue(result.isEnabled());
        verify(clientRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(employeeRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }


    @Test
    void addClient_NewClient_SavesClient() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(clientRepository.existsByEmail(CLIENT_EMAIL)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        userService.addClient(clientDTO);

        verify(clientRepository, times(1)).existsByEmail(CLIENT_EMAIL);
        verify(passwordEncoder, times(1)).encode(PASSWORD);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void addClient_ClientExists_ThrowsRuntimeException() {
        when(clientRepository.existsByEmail(CLIENT_EMAIL)).thenReturn(true);


        assertThrows(RuntimeException.class, () -> userService.addClient(clientDTO));
        verify(clientRepository, times(1)).existsByEmail(CLIENT_EMAIL);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void addEmployee_NewEmployee_SavesEmployee() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(employeeRepository.existsByEmail(EMPLOYEE_EMAIL)).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        userService.addEmployee(employeeDTO);

        verify(employeeRepository, times(1)).existsByEmail(EMPLOYEE_EMAIL);
        verify(passwordEncoder, times(1)).encode(PASSWORD);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void addEmployee_EmployeeExists_ThrowsRuntimeException() {
        when(employeeRepository.existsByEmail(EMPLOYEE_EMAIL)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.addEmployee(employeeDTO));
        verify(employeeRepository, times(1)).existsByEmail(EMPLOYEE_EMAIL);
        verify(employeeRepository, never()).save(any());
    }
}
