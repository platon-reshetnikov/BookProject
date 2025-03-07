package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.mapper.BookItemMapper;
import com.epam.rd.autocode.spring.project.mapper.OrderMapper;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.BookPriceService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final BookPriceService bookPriceService;
    //private final BookItemMapper bookItemMapper;
    private final BookRepository bookRepository;
    //private BookItemDTO bookItemDTO;

    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + clientEmail));
        List<Order> orders = orderRepository.findByClientEmail(clientEmail);
        return orders.stream()
                .map(orderMapper::toDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new NotFoundException("Employee not found with email: " + employeeEmail));
        List<Order> orders = orderRepository.findByEmployeeEmail(employeeEmail);
        return orders.stream()
                .map(orderMapper::toDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO addOrder(@Valid OrderDTO orderDTO) {
        Client client = clientRepository.findByEmail(orderDTO.getClientEmail())
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + orderDTO.getClientEmail()));

        Employee employee = employeeRepository.findByEmail(orderDTO.getEmployeeEmail())
                .orElseThrow(() -> new NotFoundException("Employee not found with email: " + orderDTO.getEmployeeEmail()));

        // Преобразуем OrderDTO в Order
        Order order = orderMapper.toEntity(orderDTO);
        order.setClient(client);
        order.setEmployee(employee);

        // Устанавливаем связь между Order и BookItem
        if (order.getBookItems() != null) {
            for (BookItem bookItem : order.getBookItems()) {
                if (bookItem.getBook() == null) {
                    throw new IllegalStateException("Book is not set in BookItem: " + bookItem);
                }
                String bookName = bookItem.getBook().getName();
                if (bookName == null || bookName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Book name cannot be null or empty in BookItem: " + bookItem);
                }
                Book book = bookRepository.findByName(bookName)
                        .orElseThrow(() -> new NotFoundException("Book not found with name: " + bookName));
                bookItem.setBook(book); // Устанавливаем книгу из базы данных
                bookItem.setOrder(order); // Устанавливаем заказ
            }
        } else {
            throw new IllegalArgumentException("Order must contain at least one book item");
        }

        // Сохраняем заказ
        Order savedOrder = orderRepository.save(order);

        // Возвращаем DTO
        return orderMapper.toDTO(savedOrder);
    }

    @Override
    public List<OrderDTO> getOrdersByOrderDate(LocalDateTime orderDate) {
        List<Order> orders = orderRepository.findByOrderDate(orderDate);
        return orders.stream()
                .map(orderMapper::toDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(order -> {
                    OrderDTO dto = orderMapper.toDTO(order);
                    if (dto.getOrderDate() == null) {
                        dto.setOrderDate(LocalDateTime.now());
                    }
                    if (dto.getClientEmail() == null && order.getClient() != null) {
                        dto.setClientEmail(order.getClient().getEmail());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO confirmOrder(String clientEmail, LocalDateTime orderDate, String employeeEmail) {
        Order order = orderRepository.findByClientEmailAndOrderDate(clientEmail, orderDate)
                .orElseThrow(() -> new NotFoundException("Order not found for client " + clientEmail + " and date " + orderDate));
        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new NotFoundException("Employee not found with email: " + employeeEmail));
        order.setEmployee(employee);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDTO(savedOrder);
    }

    @Override
    public OrderDTO findOrderByClientEmailAndOrderDate(String clientEmail, LocalDateTime orderDate) {
        Optional<Order> orderOptional = orderRepository.findByClientEmailAndOrderDate(clientEmail, orderDate);
        Order order = orderOptional.orElseThrow(() -> new NotFoundException("Order not found for client " + clientEmail + " and date " + orderDate));
        return orderMapper.toDTO(order);
    }

    @Override
    public List<Employee> getAllEmployees() {
        logger.debug("Fetching all employees");
        List<Employee> employees = employeeRepository.findAll();
        logger.debug("Found {} employees", employees.size());
        return employees;
    }

    @Override
    public OrderDTO calculateOrderPrice(OrderDTO orderDTO) {
        if (orderDTO == null || orderDTO.getBookItems() == null || orderDTO.getBookItems().isEmpty()) {
            orderDTO.setPrice(BigDecimal.ZERO);
            return orderDTO;
        }

        BigDecimal totalPrice = orderDTO.getBookItems().stream()
                .map(item -> {
                    try {
                        BigDecimal bookPrice = bookPriceService.getBookPrice(item.getBookName().trim());
                        return bookPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                    } catch (Exception e) {
                        return BigDecimal.ZERO;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        orderDTO.setPrice(totalPrice);
        return orderDTO;
    }
}
