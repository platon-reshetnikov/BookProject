package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfigTestJWT;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.service.BookPriceService;
import com.epam.rd.autocode.spring.project.service.OrderService;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(SecurityConfigTestJWT.class)

public class OrderControllerTest {
    @Qualifier("userServiceImpl")
    private UserDetailsService userDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private BookPriceService bookPriceService;

    @MockBean
    private MessageSource messageSource;

    private OrderDTO orderDTO;
    private Employee employee;
    private BookItemDTO bookItemDTO;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setEmail("employee@example.com");
        employee.setName("Test Employee");

        bookItemDTO = new BookItemDTO();
        bookItemDTO.setBookName("Test Book");
        bookItemDTO.setQuantity(1);

        orderDTO = new OrderDTO();
        orderDTO.setClientEmail("client@example.com");
        orderDTO.setEmployeeEmail("employee@example.com");
        orderDTO.setOrderDate(LocalDateTime.of(2023, 1, 1, 12, 0));
        orderDTO.setBookItems(Collections.singletonList(bookItemDTO));
        orderDTO.setPrice(BigDecimal.valueOf(29.99));

        when(messageSource.getMessage(eq("client.not.found"), any(), any(Locale.class)))
                .thenReturn("Client not found: client@example.com");
        when(messageSource.getMessage(eq("order.confirmed"), any(), any(Locale.class)))
                .thenReturn("Order confirmed");
        when(messageSource.getMessage(eq("order.not.found"), any(), any(Locale.class)))
                .thenReturn("Order not found for client@example.com at 2023-01-01T12:00");
    }

    @Test
    @WithMockUser(username = "employee@example.com", roles = "EMPLOYEE")
    void getOrdersByClient_Success_ReturnsOrdersView() throws Exception {
        List<OrderDTO> orders = Collections.singletonList(orderDTO);
        when(orderService.getOrdersByClient("client@example.com")).thenReturn(orders);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/client/client@example.com"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("orders"))
                .andExpect(MockMvcResultMatchers.model().attribute("orders", orders))
                .andExpect(MockMvcResultMatchers.model().attribute("clientEmail", "client@example.com"));
    }

    @Test
    void getAllOrders_Success_ReturnsOrdersView() throws Exception {
        List<OrderDTO> orders = Collections.singletonList(orderDTO);
        List<Employee> employees = Collections.singletonList(employee);
        when(orderService.getAllOrders()).thenReturn(orders);
        when(orderService.getAllEmployees()).thenReturn(employees);
        when(bookPriceService.getBookPrice("Test Book")).thenReturn(BigDecimal.valueOf(29.99));

        mockMvc.perform(MockMvcRequestBuilders.get("/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee@example.com").roles("EMPLOYEE")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("orders"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("orders"))
                .andExpect(MockMvcResultMatchers.model().attribute("employees", employees))
                .andExpect(MockMvcResultMatchers.model().attribute("bookPriceService", bookPriceService));
    }

    @Test
    void getAllOrders_WithBookPriceException_ReturnsOrdersViewWithZeroPrice() throws Exception {
        List<OrderDTO> orders = Collections.singletonList(orderDTO);
        List<Employee> employees = Collections.singletonList(employee);
        when(orderService.getAllOrders()).thenReturn(orders);
        when(orderService.getAllEmployees()).thenReturn(employees);
        when(bookPriceService.getBookPrice("Test Book")).thenThrow(new RuntimeException("Price not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee@example.com").roles("EMPLOYEE")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("orders"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("orders"))
                .andExpect(MockMvcResultMatchers.model().attribute("employees", employees));
    }

    @Test
    void confirmOrder_Success_RedirectsToOrders() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/orders/confirm")
                        .param("clientEmail", "client@example.com")
                        .param("orderDate", "2023-01-01T12:00:00")
                        .param("employeeEmail", "employee@example.com")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee@example.com").roles("EMPLOYEE"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/orders"))
                .andExpect(MockMvcResultMatchers.flash().attribute("successMessage", "Order confirmed"));

        verify(orderService, times(1)).confirmOrder(eq("client@example.com"), eq(LocalDateTime.of(2023, 1, 1, 12, 0)), eq("employee@example.com"));
    }

    @Test
    void confirmOrder_NotFound_RedirectsToOrdersWithError() throws Exception {
        doThrow(new NotFoundException("Order not found")).when(orderService)
                .confirmOrder(eq("client@example.com"), eq(LocalDateTime.of(2023, 1, 1, 12, 0)), eq("employee@example.com"));

        mockMvc.perform(MockMvcRequestBuilders.post("/orders/confirm")
                        .param("clientEmail", "client@example.com")
                        .param("orderDate", "2023-01-01T12:00:00")
                        .param("employeeEmail", "employee@example.com")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee@example.com").roles("EMPLOYEE"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/orders"))
                .andExpect(MockMvcResultMatchers.flash().attribute("errorMessage", "Order not found for client@example.com at 2023-01-01T12:00"));

        verify(orderService, times(1)).confirmOrder(eq("client@example.com"), eq(LocalDateTime.of(2023, 1, 1, 12, 0)), eq("employee@example.com"));
    }
}
