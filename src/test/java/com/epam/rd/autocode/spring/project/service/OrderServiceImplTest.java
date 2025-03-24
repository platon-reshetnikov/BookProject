package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.OrderMapper;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BookService bookService;

    @Mock
    private BookPriceService bookPriceService;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Client client;
    private Employee employee;
    private Order order;
    private OrderDTO orderDTO;
    private Book book;
    private BookItem bookItem;
    private static final String CLIENT_EMAIL = "client@example.com";
    private static final String EMPLOYEE_EMAIL = "employee@example.com";
    private static final LocalDateTime ORDER_DATE = LocalDateTime.now();


    @BeforeEach
    void setUp() {
        client = new Client();
        client.setEmail(CLIENT_EMAIL);

        employee = new Employee();
        employee.setEmail(EMPLOYEE_EMAIL);

        book = new Book();
        book.setName("Test Book");

        bookItem = new BookItem();
        bookItem.setBook(book);
        bookItem.setQuantity(2);

        order = new Order();
        order.setClient(client);
        order.setEmployee(employee);
        order.setOrderDate(ORDER_DATE);
        order.setBookItems(Arrays.asList(bookItem));

        orderDTO = new OrderDTO();
        orderDTO.setClientEmail(CLIENT_EMAIL);
        orderDTO.setEmployeeEmail(EMPLOYEE_EMAIL);
        orderDTO.setOrderDate(ORDER_DATE);
        orderDTO.setBookItems(Arrays.asList(new BookItemDTO("Test Book", 2)));
    }

    @Test
    void getOrdersByClient_ClientExists_ReturnsOrders(){
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(orderRepository.findByClientEmail(CLIENT_EMAIL)).thenReturn(Arrays.asList(order));
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        List<OrderDTO> result = orderService.getOrdersByClient(CLIENT_EMAIL);

        assertEquals(1, result.size());
        assertEquals(orderDTO, result.get(0));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(orderRepository, times(1)).findByClientEmail(CLIENT_EMAIL);
        verify(orderMapper, times(1)).toDTO(order);
    }

    @Test
    void getOrdersByClient_ClientNotFound_ThrowsNotFoundException(){
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrdersByClient(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(orderRepository, never()).findByClientEmail(anyString());
    }

    @Test
    void getOrdersByEmployee_EmployeeExists_ReturnsOrders(){
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(employee));
        when(orderRepository.findByEmployeeEmail(EMPLOYEE_EMAIL)).thenReturn(Arrays.asList(order));
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        List<OrderDTO> result = orderService.getOrdersByEmployee(EMPLOYEE_EMAIL);

        assertEquals(1, result.size());
        assertEquals(orderDTO, result.get(0));
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
        verify(orderRepository, times(1)).findByEmployeeEmail(EMPLOYEE_EMAIL);
        verify(orderMapper, times(1)).toDTO(order);
    }

    @Test
    void getOrdersByEmployee_EmployeeNotFound_ThrowsNotFoundException(){
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrdersByEmployee(EMPLOYEE_EMAIL));
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
        verify(orderRepository, never()).findByEmployeeEmail(anyString());

    }

    @Test
    void addOrder_ValidOrder_ReturnsOrderDTO(){
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(employee));
        when(orderMapper.toEntity(orderDTO)).thenReturn(order);
        when(bookRepository.findByName("Test Book")).thenReturn(Optional.of(book));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.addOrder(orderDTO);

        assertEquals(orderDTO, result);
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
        verify(bookRepository, times(1)).findByName("Test Book");
        verify(orderRepository, times(1)).save(order);
        verify(orderMapper, times(1)).toDTO(order);
    }

    @Test
    void addOrder_ClientNotFound_ThrowsNotFoundException(){
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.addOrder(orderDTO));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(employeeRepository, never()).findByEmail(anyString());
    }

    @Test
    void addOrder_EmployeeNotFound_ThrowsNotFoundException(){
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.addOrder(orderDTO));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
    }

    @Test
    void addOrder_BookNotFound_ThrowsNotFoundException(){
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(employee));
        when(orderMapper.toEntity(orderDTO)).thenReturn(order);
        when(bookRepository.findByName("Test Book")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.addOrder(orderDTO));
        verify(bookRepository, times(1)).findByName("Test Book");
    }

    @Test
    void addOrder_NoBookItems_ThrowsIllegalArgumentException() {
        orderDTO.setBookItems(null);
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(employee));

        Order orderWithNoBookItems = new Order();
        orderWithNoBookItems.setClient(client);
        orderWithNoBookItems.setEmployee(employee);
        orderWithNoBookItems.setOrderDate(ORDER_DATE);
        orderWithNoBookItems.setBookItems(null);

        when(orderMapper.toEntity(orderDTO)).thenReturn(orderWithNoBookItems);

        assertThrows(IllegalArgumentException.class, () -> orderService.addOrder(orderDTO));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getAllOrders_ReturnsOrders(){
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        List<OrderDTO> result = orderService.getAllOrders();
        assertEquals(1, result.size());
        assertEquals(orderDTO, result.get(0));
        verify(orderRepository, times(1)).findAll();
        verify(orderMapper, times(1)).toDTO(order);
    }

    @Test
    void getAllOrders_EmptyList_ReturnsEmptyList(){
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        List<OrderDTO> result = orderService.getAllOrders();
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findAll();
        verify(orderMapper, never()).toDTO(any());
    }

    @Test
    void confirmOrder_OrderExists_ReturnsUpdatedOrderDTO(){
        when(orderRepository.findByClientEmailAndOrderDate(CLIENT_EMAIL, ORDER_DATE)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(employee));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.confirmOrder(CLIENT_EMAIL, ORDER_DATE, EMPLOYEE_EMAIL);
        assertEquals(orderDTO, result);
        verify(orderRepository, times(1)).findByClientEmailAndOrderDate(CLIENT_EMAIL, ORDER_DATE);
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
        verify(orderRepository, times(1)).save(order);
        verify(orderMapper, times(1)).toDTO(order);
    }

    @Test
    void confirmOrder_OrderNotFound_ThrowsNotFoundException(){
        when(orderRepository.findByClientEmailAndOrderDate(CLIENT_EMAIL, ORDER_DATE)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.confirmOrder(CLIENT_EMAIL, ORDER_DATE, EMPLOYEE_EMAIL));
        verify(orderRepository, times(1)).findByClientEmailAndOrderDate(CLIENT_EMAIL, ORDER_DATE);
        verify(employeeRepository, never()).findByEmail(anyString());
    }

    @Test
    void confirmOrder_EmployeeNotFound_ThrowsNotFoundException(){
        when(orderRepository.findByClientEmailAndOrderDate(CLIENT_EMAIL, ORDER_DATE)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.confirmOrder(CLIENT_EMAIL, ORDER_DATE, EMPLOYEE_EMAIL));
        verify(orderRepository, times(1)).findByClientEmailAndOrderDate(CLIENT_EMAIL, ORDER_DATE);
        verify(employeeRepository, times(1)).findByEmail(EMPLOYEE_EMAIL);
    }

    @Test
    void getAllEmployees_ReturnsEmployees(){
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(employee));

        List<Employee> result = orderService.getAllEmployees();
        assertEquals(1, result.size());
        assertEquals(employee, result.get(0));
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void getAllEmployees_EmptyList_ReturnsEmptyList(){
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Employee> result = orderService.getAllEmployees();
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void calculateOrderPrice_ValidOrder_ReturnsOrderWithPrice(){
        when(bookPriceService.getBookPrice("Test Book")).thenReturn(new BigDecimal("10.00"));

        OrderDTO result = orderService.calculateOrderPrice(orderDTO);
        assertEquals(new BigDecimal("20.00"), result.getPrice());
        verify(bookPriceService, times(1)).getBookPrice("Test Book");
    }

    @Test
    void calculateOrderPrice_NullOrder_SetsPriceToZero() {
        // Act
        OrderDTO result = orderService.calculateOrderPrice(null);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getPrice());
        verify(bookPriceService, never()).getBookPrice(anyString());
    }

    @Test
    void calculateOrderPrice_EmptyBookItems_SetsPriceToZero(){
        orderDTO.setBookItems(Collections.emptyList());

        OrderDTO result = orderService.calculateOrderPrice(orderDTO);
        assertEquals(BigDecimal.ZERO, result.getPrice());
        verify(bookPriceService, never()).getBookPrice(anyString());
    }

    @Test
    void calculateOrderPrice_BookPriceServiceThrowsException_SetsPriceToZeroForItem(){
        when(bookPriceService.getBookPrice("Test Book")).thenThrow(new RuntimeException("Price not found"));

        OrderDTO result = orderService.calculateOrderPrice(orderDTO);
        assertEquals(BigDecimal.ZERO, result.getPrice());
        verify(bookPriceService, times(1)).getBookPrice("Test Book");
    }
}
