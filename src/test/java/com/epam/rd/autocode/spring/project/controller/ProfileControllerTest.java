package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfigTestJWT;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.mapper.UserWrapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.service.UserService;
import com.epam.rd.autocode.spring.project.service.impl.UserServiceImpl;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebMvcTest(ProfileController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(SecurityConfigTestJWT.class)
public class ProfileControllerTest {
    @Qualifier("userServiceImpl")
    private UserDetailsService userDetailsService;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private MessageSource messageSource;
    private Client client;
    private Employee employee;
    private ClientDTO clientDTO;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setEmail("client@example.com");
        client.setName("Test Client");
        client.setPassword("password");
        client.setBalance(BigDecimal.valueOf(100));

        employee = new Employee();
        employee.setEmail("employee@example.com");
        employee.setName("Test Employee");
        employee.setPassword("password");
        employee.setPhone("1234567890");
        employee.setBirthDate(LocalDate.of(1990, 1, 1));

        clientDTO = new ClientDTO("client@example.com", "password", "Test Client", BigDecimal.valueOf(100));
        employeeDTO = new EmployeeDTO("employee@example.com", "password", "Test Employee", "1234567890", LocalDate.of(1990, 1, 1));

        when(messageSource.getMessage(eq("profile.client.missing"), any(), any(Locale.class)))
                .thenReturn("Client data is missing");
        when(messageSource.getMessage(eq("profile.employee.missing"), any(), any(Locale.class)))
                .thenReturn("Employee data is missing");
        when(messageSource.getMessage(eq("profile.save.error"), any(), any(Locale.class)))
                .thenReturn("Error saving profile: some error");
        when(messageSource.getMessage(eq("profile.invalid.type"), any(), any(Locale.class)))
                .thenReturn("Invalid user type: unknown");
    }

    @Test
    void viewProfile_Client_ReturnsProfileView() throws Exception {
        when(userService.getClientByEmail("client@example.com")).thenReturn(client);

        mockMvc.perform(MockMvcRequestBuilders.get("/profile")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("profile"))
                .andExpect(MockMvcResultMatchers.model().attribute("user", client))
                .andExpect(MockMvcResultMatchers.model().attribute("userType", "client"));
    }

    @Test
    void viewProfile_Employee_ReturnsProfileView() throws Exception {
        when(userService.getEmployeeByEmail("employee@example.com")).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/profile")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee@example.com").roles("EMPLOYEE")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("profile"))
                .andExpect(MockMvcResultMatchers.model().attribute("user", employee))
                .andExpect(MockMvcResultMatchers.model().attribute("userType", "employee"));
    }

    @Test
    void editProfileForm_Client_ReturnsEditProfileView() throws Exception {
        when(userService.getClientByEmail("client@example.com")).thenReturn(client);

        mockMvc.perform(MockMvcRequestBuilders.get("/profile/edit")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("edit-profile"))
                .andExpect(MockMvcResultMatchers.model().attribute("userType", "client"))
                .andExpect(MockMvcResultMatchers.model().attribute("userWrapper", hasProperty("clientDTO", hasProperty("email", is("client@example.com")))));
    }

    @Test
    void editProfileForm_Employee_ReturnsEditProfileView() throws Exception {
        when(userService.getEmployeeByEmail("employee@example.com")).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/profile/edit")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee@example.com").roles("EMPLOYEE")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("edit-profile"))
                .andExpect(MockMvcResultMatchers.model().attribute("userType", "employee"))
                .andExpect(MockMvcResultMatchers.model().attribute("formattedBirthDate", "1990-01-01"))
                .andExpect(MockMvcResultMatchers.model().attribute("userWrapper", hasProperty("employeeDTO", hasProperty("email", is("employee@example.com")))));
    }

    @Test
    void saveProfile_Client_Success_RedirectsToProfile() throws Exception {
        UserWrapper userWrapper = new UserWrapper();
        userWrapper.setClientDTO(clientDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/profile/edit")
                        .param("userType", "client")
                        .flashAttr("userWrapper", userWrapper)
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/profile"));

        verify(userService, times(1)).updateClient(eq("client@example.com"), eq(clientDTO));
    }

    @Test
    void saveProfile_Employee_Success_RedirectsToProfile() throws Exception {
        UserWrapper userWrapper = new UserWrapper();
        userWrapper.setEmployeeDTO(employeeDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/profile/edit")
                        .param("userType", "employee")
                        .flashAttr("userWrapper", userWrapper)
                        .with(SecurityMockMvcRequestPostProcessors.user("employee@example.com").roles("EMPLOYEE"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/profile"));

        verify(userService, times(1)).updateEmployee(eq("employee@example.com"), eq(employeeDTO));
    }
}
