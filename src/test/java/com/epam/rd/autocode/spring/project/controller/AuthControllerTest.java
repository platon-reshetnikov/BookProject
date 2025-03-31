package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfigTestJWT;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.mapper.UserWrapper;
import com.epam.rd.autocode.spring.project.service.UserService;
import com.epam.rd.autocode.spring.project.service.impl.UserServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import validation.ClientValidationGroup;
import validation.EmployeeValidationGroup;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@WithMockUser
@Import(SecurityConfigTestJWT.class)

public class AuthControllerTest {
    @Qualifier("userServiceImpl")
    private UserDetailsService userDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private Validator validator;

    @MockBean
    private MessageSource messageSource;

    private ClientDTO clientDTO;
    private EmployeeDTO employeeDTO;
    private UserWrapper userWrapper;

    @BeforeEach
    void setUp() {
        clientDTO = new ClientDTO();
        clientDTO.setEmail("client@example.com");
        clientDTO.setPassword("password123");
        clientDTO.setName("Client Name");
        clientDTO.setBalance(BigDecimal.valueOf(100));

        employeeDTO = new EmployeeDTO();
        employeeDTO.setEmail("employee@example.com");
        employeeDTO.setPassword("password123");
        employeeDTO.setName("Employee Name");
        employeeDTO.setPhone("1234567890");
        employeeDTO.setBirthDate(LocalDate.of(1990, 1, 1));

        userWrapper = new UserWrapper();
        userWrapper.setClientDTO(clientDTO);
        userWrapper.setEmployeeDTO(employeeDTO);

        when(messageSource.getMessage(eq("register.client.success"), any(), any(Locale.class)))
                .thenReturn("Client registered successfully");
        when(messageSource.getMessage(eq("register.employee.success"), any(), any(Locale.class)))
                .thenReturn("Employee registered successfully");
    }

    @Test
    void showRegistrationForm_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/register"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("userWrapper"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", false));
    }

    @Test
    void registerClient_ValidData_ShouldReturnSuccess() throws Exception {
        when(validator.validate(any(ClientDTO.class), eq(ClientValidationGroup.class)))
                .thenReturn(Collections.emptySet());

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("userType", "client")
                        .flashAttr("userWrapper", userWrapper)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())) // Если CSRF включен
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", true))
                .andExpect(MockMvcResultMatchers.model().attribute("successMessage", "Client registered successfully"));

        verify(userService, times(1)).addClient(clientDTO);
    }

    @Test
    void registerEmployee_ValidData_ShouldReturnSuccess() throws Exception {
        when(validator.validate(any(EmployeeDTO.class), eq(EmployeeValidationGroup.class)))
                .thenReturn(Collections.emptySet());

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("userType", "employee")
                        .flashAttr("userWrapper", userWrapper)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", true))
                .andExpect(MockMvcResultMatchers.model().attribute("successMessage", "Employee registered successfully"));

        verify(userService, times(1)).addEmployee(employeeDTO);
    }

}
