package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.mapper.UserWrapper;
import com.epam.rd.autocode.spring.project.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
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

@WebMvcTest(value = AuthController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthControllerTest {

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
        when(messageSource.getMessage(eq("register.duplicate.error"), any(), any(Locale.class)))
                .thenReturn("User with email %s already exists");
        when(messageSource.getMessage(eq("register.error"), any(), any(Locale.class)))
                .thenReturn("Registration failed: %s");
        when(messageSource.getMessage(eq("register.invalid.type"), any(), any(Locale.class)))
                .thenReturn("Invalid user type");
        when(messageSource.getMessage(eq("validation.user.type"), any(), any(Locale.class)))
                .thenReturn("Please select a user type");
    }

    @Test
    void showRegistrationForm_ReturnsRegisterView() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/register"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("userWrapper"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", false));
    }

    @Test
    void registerUser_ClientSuccess_ReturnsRegisterViewWithSuccessMessage() throws Exception {
        when(validator.validate(any(ClientDTO.class), eq(ClientValidationGroup.class)))
                .thenReturn(Collections.emptySet());

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("userType", "client")
                        .flashAttr("userWrapper", userWrapper))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", true))
                .andExpect(MockMvcResultMatchers.model().attribute("successMessage", "Client registered successfully"));

        verify(userService, times(1)).addClient(clientDTO);
        verify(userService, never()).addEmployee(any());
    }

    @Test
    void registerUser_EmployeeSuccess_ReturnsRegisterViewWithSuccessMessage() throws Exception {
        when(validator.validate(any(EmployeeDTO.class), eq(EmployeeValidationGroup.class)))
                .thenReturn(Collections.emptySet());

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("userType", "employee")
                        .flashAttr("userWrapper", userWrapper))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", true))
                .andExpect(MockMvcResultMatchers.model().attribute("successMessage", "Employee registered successfully"));

        verify(userService, times(1)).addEmployee(employeeDTO);
        verify(userService, never()).addClient(any());
    }

    @Test
    void registerUser_ClientValidationErrors_ReturnsRegisterViewWithErrors() throws Exception {
        ConstraintViolation<ClientDTO> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("email");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessageTemplate()).thenReturn("validation.email");
        when(violation.getMessage()).thenReturn("Invalid email");
        when(validator.validate(any(ClientDTO.class), eq(ClientValidationGroup.class)))
                .thenReturn(Set.of(violation));

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("userType", "client")
                        .flashAttr("userWrapper", userWrapper))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", true))
                .andExpect(MockMvcResultMatchers.model().attributeHasFieldErrors("userWrapper", "clientDTO.email"));

        verify(userService, never()).addClient(any());
        verify(userService, never()).addEmployee(any());
    }

    @Test
    void registerUser_EmployeeValidationErrors_ReturnsRegisterViewWithErrors() throws Exception {
        ConstraintViolation<EmployeeDTO> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("email");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessageTemplate()).thenReturn("validation.email");
        when(violation.getMessage()).thenReturn("Invalid email");
        when(validator.validate(any(EmployeeDTO.class), eq(EmployeeValidationGroup.class)))
                .thenReturn(Set.of(violation));

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("userType", "employee")
                        .flashAttr("userWrapper", userWrapper))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", true))
                .andExpect(MockMvcResultMatchers.model().attributeHasFieldErrors("userWrapper", "employeeDTO.email"));

        verify(userService, never()).addClient(any());
        verify(userService, never()).addEmployee(any());
    }

    @Test
    void registerUser_ClientDuplicateEmail_ReturnsRegisterViewWithError() throws Exception {
        when(validator.validate(any(ClientDTO.class), eq(ClientValidationGroup.class)))
                .thenReturn(Collections.emptySet());
        doThrow(new RuntimeException("Client with email client@example.com already exists"))
                .when(userService).addClient(clientDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("userType", "client")
                        .flashAttr("userWrapper", userWrapper))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", true))
                .andExpect(MockMvcResultMatchers.model().attribute("error", "User with email client@example.com already exists"));

        verify(userService, times(1)).addClient(clientDTO);
        verify(userService, never()).addEmployee(any());
    }

    @Test
    void registerUser_EmployeeDuplicateEmail_ReturnsRegisterViewWithError() throws Exception {
        when(validator.validate(any(EmployeeDTO.class), eq(EmployeeValidationGroup.class)))
                .thenReturn(Collections.emptySet());
        doThrow(new RuntimeException("Employee with email employee@example.com already exists"))
                .when(userService).addEmployee(employeeDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("userType", "employee")
                        .flashAttr("userWrapper", userWrapper))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", true))
                .andExpect(MockMvcResultMatchers.model().attribute("error", "User with email employee@example.com already exists"));

        verify(userService, times(1)).addEmployee(employeeDTO);
        verify(userService, never()).addClient(any());
    }

    @Test
    void registerUser_InvalidUserType_ReturnsRegisterViewWithError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("userType", "invalid")
                        .flashAttr("userWrapper", userWrapper))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", true))
                .andExpect(MockMvcResultMatchers.model().attributeHasFieldErrors("userWrapper", "userType"));

        verify(userService, never()).addClient(any());
        verify(userService, never()).addEmployee(any());
    }

    @Test
    void registerUser_WithLangParameter_UsesCorrectLocale() throws Exception {
        when(validator.validate(any(ClientDTO.class), eq(ClientValidationGroup.class)))
                .thenReturn(Collections.emptySet());
        when(messageSource.getMessage(eq("register.client.success"), any(), eq(new Locale("fr"))))
                .thenReturn("Client enregistré avec succès");

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("userType", "client")
                        .param("lang", "fr")
                        .flashAttr("userWrapper", userWrapper))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attribute("submitted", true))
                .andExpect(MockMvcResultMatchers.model().attribute("successMessage", "Client enregistré avec succès"));

        verify(userService, times(1)).addClient(clientDTO);
    }

    @Test
    void oauth2Success_ReturnsBooksViewWithSuccessMessage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/register/oauth2/success"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("books"))
                .andExpect(MockMvcResultMatchers.model().attribute("successMessage", "You have successfully logged in with Google!"));
    }
}
