package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebMvcTest(ClientController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private MessageSource messageSource;

    private ClientDTO clientDTO;
    private BookItemDTO bookItemDTO;
    private Employee employee;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        clientDTO = new ClientDTO();
        clientDTO.setEmail("client@example.com");
        clientDTO.setName("Test Client");
        clientDTO.setBalance(BigDecimal.valueOf(100));

        bookItemDTO = new BookItemDTO();
        bookItemDTO.setBookName("Test Book");
        bookItemDTO.setQuantity(1);

        employee = new Employee();
        employee.setEmail("employee@example.com");
        employee.setName("Test Employee");

        orderDTO = new OrderDTO();
        orderDTO.setClientEmail("client@example.com");
        orderDTO.setEmployeeEmail("employee@example.com");
        orderDTO.setOrderDate(LocalDateTime.now());
        orderDTO.setBookItems(Collections.singletonList(bookItemDTO));
        orderDTO.setPrice(BigDecimal.valueOf(29.99));

        when(messageSource.getMessage(eq("client.blocked"), any(), any(Locale.class)))
                .thenReturn("Client client@example.com has been blocked");
        when(messageSource.getMessage(eq("client.unblocked"), any(), any(Locale.class)))
                .thenReturn("Client client@example.com has been unblocked");
        when(messageSource.getMessage(eq("client.not.found"), any(), any(Locale.class)))
                .thenReturn("Client not found: client@example.com");
        when(messageSource.getMessage(eq("basket.added"), any(), any(Locale.class)))
                .thenReturn("Book Test Book added to basket");
        when(messageSource.getMessage(eq("basket.cleared"), any(), any(Locale.class)))
                .thenReturn("Basket cleared");
        when(messageSource.getMessage(eq("order.submitted"), any(), any(Locale.class)))
                .thenReturn("Order submitted to employee@example.com");
        when(messageSource.getMessage(eq("basket.empty"), any(), any(Locale.class)))
                .thenReturn("Basket is empty");
        when(messageSource.getMessage(eq("order.no.employees"), any(), any(Locale.class)))
                .thenReturn("No employees available");
        when(messageSource.getMessage(eq("order.error"), any(), any(Locale.class)))
                .thenReturn("Order error: some error");
        when(messageSource.getMessage(eq("client.not.exists"), any(), any(Locale.class)))
                .thenReturn("Client does not exist");
    }

    @Test
    void getAllClients_ReturnsClientsView() throws Exception {
        List<ClientDTO> clients = Collections.singletonList(clientDTO);
        when(clientService.getAllClients()).thenReturn(clients);
        when(clientService.isClientBlocked("client@example.com")).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/clients/manage")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee").roles("EMPLOYEE")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("clients"))
                .andExpect(MockMvcResultMatchers.model().attribute("clients", clients))
                .andExpect(MockMvcResultMatchers.model().attribute("clientBlockedStatus", new HashMap<String, Boolean>() {{
                    put("client@example.com", false);
                }}));
    }

    @Test
    void blockClient_Success_RedirectsToManage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/clients/block/client@example.com")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee").roles("EMPLOYEE")))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/clients/manage"))
                .andExpect(MockMvcResultMatchers.model().attribute("successMessage", "Client client@example.com has been blocked"));

        verify(clientService, times(1)).blockClient("client@example.com");
    }

    @Test
    void blockClient_NotFound_RedirectsToManageWithError() throws Exception {
        doThrow(new NotFoundException("Client not found")).when(clientService).blockClient("unknown@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/clients/block/unknown@example.com")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee").roles("EMPLOYEE")))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/clients/manage"))
                .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Client not found: client@example.com"));

        verify(clientService, times(1)).blockClient("unknown@example.com");
    }

    @Test
    void unblockClient_Success_RedirectsToManage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/clients/unblock/client@example.com")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee").roles("EMPLOYEE")))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/clients/manage"))
                .andExpect(MockMvcResultMatchers.model().attribute("successMessage", "Client client@example.com has been unblocked"));

        verify(clientService, times(1)).unblockClient("client@example.com");
    }

    @Test
    void unblockClient_NotFound_RedirectsToManageWithError() throws Exception {
        doThrow(new NotFoundException("Client not found")).when(clientService).unblockClient("unknown@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/clients/unblock/unknown@example.com")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee").roles("EMPLOYEE")))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/clients/manage"))
                .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Client not found: client@example.com"));

        verify(clientService, times(1)).unblockClient("unknown@example.com");
    }

    @Test
    void getClientList_ReturnsClientListView() throws Exception {
        List<ClientDTO> clients = Collections.singletonList(clientDTO);
        when(clientService.getAllClients()).thenReturn(clients);

        mockMvc.perform(MockMvcRequestBuilders.get("/clients/list")
                        .with(SecurityMockMvcRequestPostProcessors.user("employee").roles("EMPLOYEE")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("client-list"))
                .andExpect(MockMvcResultMatchers.model().attribute("clients", clients));
    }

    @Test
    void addBookToBasket_Success_RedirectsToBooks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/clients/basket/add/Test Book")
                        .param("quantity", "1")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/books"))
                .andExpect(MockMvcResultMatchers.model().attribute("successMessage", "Book Test Book added to basket"));

        verify(clientService, times(1)).addBookToBasket("client@example.com", "Test Book", 1);
    }

    @Test
    void addBookToBasket_NotFound_RedirectsToBooksWithError() throws Exception {
        doThrow(new NotFoundException("Client not found")).when(clientService).addBookToBasket("client@example.com", "Test Book", 1);

        mockMvc.perform(MockMvcRequestBuilders.post("/clients/basket/add/Test Book")
                        .param("quantity", "1")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/books"))
                .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Client not found: client@example.com"));

        verify(clientService, times(1)).addBookToBasket("client@example.com", "Test Book", 1);
    }

    @Test
    void viewBasket_ReturnsBasketView() throws Exception {
        List<BookItemDTO> basket = Collections.singletonList(bookItemDTO);
        when(clientService.getBasket("client@example.com")).thenReturn(basket);

        mockMvc.perform(MockMvcRequestBuilders.get("/clients/basket")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("basket"))
                .andExpect(MockMvcResultMatchers.model().attribute("basket", basket));
    }

    @Test
    void clearBasket_Success_RedirectsToBasket() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/clients/basket/clear")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/clients/basket"))
                .andExpect(MockMvcResultMatchers.model().attribute("successMessage", "Basket cleared"));

        verify(clientService, times(1)).clearBasket("client@example.com");
    }

    @Test
    void clearBasket_NotFound_RedirectsToBasketWithError() throws Exception {
        doThrow(new NotFoundException("Client not found")).when(clientService).clearBasket("client@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/clients/basket/clear")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/clients/basket"))
                .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Client not found: client@example.com"));

        verify(clientService, times(1)).clearBasket("client@example.com");
    }

    @Test
    void deleteAccount_Success_RedirectsToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/clients/delete")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login?deleted"));

        verify(clientService, times(1)).deleteClientByEmail("client@example.com");
    }

    @Test
    void deleteAccount_NotFound_ReturnsProfileWithError() throws Exception {
        doThrow(new NotFoundException("Client not found")).when(clientService).deleteClientByEmail("client@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/clients/delete")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("profile"))
                .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Client not found: client@example.com"));

        verify(clientService, times(1)).deleteClientByEmail("client@example.com");
    }

    @Test
    void submitOrder_Success_RedirectsToBasket() throws Exception {
        when(clientService.getClientByEmail("client@example.com")).thenReturn(clientDTO);
        when(clientService.getBasket("client@example.com")).thenReturn(Collections.singletonList(bookItemDTO));
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(employee));
        when(orderService.calculateOrderPrice(any(OrderDTO.class))).thenReturn(orderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/clients/basket/submit")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/clients/basket"))
                .andExpect(MockMvcResultMatchers.model().attribute("successMessage", "Order submitted to employee@example.com"));

        verify(orderService, times(1)).addOrder(any(OrderDTO.class));
        verify(clientService, times(1)).clearBasket("client@example.com");
    }

    @Test
    void submitOrder_ClientNotFound_RedirectsToLogin() throws Exception {
        doThrow(new NotFoundException("Client not found")).when(clientService).getClientByEmail("client@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/clients/basket/submit")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login?error"))
                .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Client does not exist"));
    }

    @Test
    void submitOrder_EmptyBasket_ReturnsBasketWithError() throws Exception {
        when(clientService.getClientByEmail("client@example.com")).thenReturn(clientDTO);
        when(clientService.getBasket("client@example.com")).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.post("/clients/basket/submit")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("basket"))
                .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Basket is empty"));

        verify(orderService, never()).addOrder(any());
    }

    @Test
    void submitOrder_NoEmployees_ReturnsBasketWithError() throws Exception {
        when(clientService.getClientByEmail("client@example.com")).thenReturn(clientDTO);
        when(clientService.getBasket("client@example.com")).thenReturn(Collections.singletonList(bookItemDTO));
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.post("/clients/basket/submit")
                        .with(SecurityMockMvcRequestPostProcessors.user("client@example.com").roles("CLIENT")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("basket"))
                .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "No employees available"));

        verify(orderService, never()).addOrder(any());
    }
}