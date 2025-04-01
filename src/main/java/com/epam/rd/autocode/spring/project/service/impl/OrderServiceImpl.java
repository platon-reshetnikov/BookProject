package com.epam.rd.autocode.spring.project.service.impl;

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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
    private final BookRepository bookRepository;

    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        logger.info("Retrieving orders for client: {}", clientEmail);

        clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> {
                    logger.warn("Client not found with email: {}", clientEmail);
                    return new NotFoundException("Client not found with email: " + clientEmail);
                });

        List<Order> orders = orderRepository.findByClientEmail(clientEmail);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(orderMapper::toDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        logger.debug("Retrieved {} orders for client: {}", orderDTOs.size(), clientEmail);
        return orderDTOs;
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        logger.info("Retrieving orders for employee: {}", employeeEmail);

        employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> {
                    logger.warn("Employee not found with email: {}", employeeEmail);
                    return new NotFoundException("Employee not found with email: " + employeeEmail);
                });

        List<Order> orders = orderRepository.findByEmployeeEmail(employeeEmail);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(orderMapper::toDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        logger.debug("Retrieved {} orders for employee: {}", orderDTOs.size(), employeeEmail);
        return orderDTOs;
    }

    @Override
    public OrderDTO addOrder(@Valid OrderDTO orderDTO) {
        logger.info("Adding new order: {}", orderDTO);

        Client client = clientRepository.findByEmail(orderDTO.getClientEmail())
                .orElseThrow(() -> {
                    logger.warn("Client not found with email: {}", orderDTO.getClientEmail());
                    return new NotFoundException("Client not found with email: " + orderDTO.getClientEmail());
                });

        Employee employee = employeeRepository.findByEmail(orderDTO.getEmployeeEmail())
                .orElseThrow(() -> {
                    logger.warn("Employee not found with email: {}", orderDTO.getEmployeeEmail());
                    return new NotFoundException("Employee not found with email: " + orderDTO.getEmployeeEmail());
                });

        Order order = orderMapper.toEntity(orderDTO);
        order.setClient(client);
        order.setEmployee(employee);

        if (order.getBookItems() != null) {
            for (BookItem bookItem : order.getBookItems()) {
                if (bookItem.getBook() == null) {
                    logger.error("Book is not set in BookItem: {}", bookItem);
                    throw new IllegalStateException("Book is not set in BookItem: " + bookItem);
                }
                String bookName = bookItem.getBook().getName();
                if (bookName == null || bookName.trim().isEmpty()) {
                    logger.warn("Book name is null or empty in BookItem: {}", bookItem);
                    throw new IllegalArgumentException("Book name cannot be null or empty in BookItem: " + bookItem);
                }
                Book book = bookRepository.findByName(bookName)
                        .orElseThrow(() -> {
                            logger.warn("Book not found with name: {}", bookName);
                            return new NotFoundException("Book not found with name: " + bookName);
                        });
                bookItem.setBook(book);
                bookItem.setOrder(order);
                logger.debug("Linked book item - Book: {}, Quantity: {}", bookName, bookItem.getQuantity());
            }
        } else {
            logger.warn("Order has no book items: {}", orderDTO);
            throw new IllegalArgumentException("Order must contain at least one book item");
        }

        Order savedOrder = orderRepository.save(order);
        logger.info("Order added successfully - Client: {}, Date: {}", savedOrder.getClient().getEmail(), savedOrder.getOrderDate());
        logger.debug("Saved order details: {}", savedOrder);
        return orderMapper.toDTO(savedOrder);
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        logger.info("Retrieving all orders");

        List<Order> orders = orderRepository.findAll();
        List<OrderDTO> orderDTOs = orders.stream()
                .map(order -> {
                    OrderDTO dto = orderMapper.toDTO(order);
                    if (dto.getOrderDate() == null) {
                        dto.setOrderDate(LocalDateTime.now());
                        logger.debug("Set default order date to now for order: {}", dto);
                    }
                    if (dto.getClientEmail() == null && order.getClient() != null) {
                        dto.setClientEmail(order.getClient().getEmail());
                        logger.debug("Set client email from entity: {}", dto.getClientEmail());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        logger.debug("Retrieved {} orders total", orderDTOs.size());
        return orderDTOs;
    }

    @Override
    public OrderDTO confirmOrder(String clientEmail, LocalDateTime orderDate, String employeeEmail) {
        logger.info("Confirming and removing order - Client: {}, Date: {}, Employee: {}", clientEmail, orderDate, employeeEmail);

        Order order = orderRepository.findByClientEmailAndOrderDate(clientEmail, orderDate)
                .orElseThrow(() -> {
                    logger.warn("Order not found for client: {} at date: {}", clientEmail, orderDate);
                    return new NotFoundException("Order not found for client " + clientEmail + " and date " + orderDate);
                });

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> {
                    logger.warn("Employee not found with email: {}", employeeEmail);
                    return new NotFoundException("Employee not found with email: " + employeeEmail);
                });

        // Логируем данные заказа перед удалением
        logger.debug("Order to be confirmed and removed: {}", order);

        // Удаляем заказ вместо сохранения
        orderRepository.delete(order);

        // Создаем DTO для возврата (до удаления могли бы сохранить данные в переменную, но здесь просто возвращаем исходный DTO)
        OrderDTO confirmedOrderDTO = orderMapper.toDTO(order);
        confirmedOrderDTO.setEmployeeEmail(employeeEmail); // Устанавливаем сотрудника в DTO для возврата

        logger.info("Order confirmed and removed successfully - Client: {}, Date: {}", clientEmail, orderDate);
        return confirmedOrderDTO;
    }

    @Override
    public List<Employee> getAllEmployees() {
        logger.info("Retrieving all employees");

        List<Employee> employees = employeeRepository.findAll();
        logger.debug("Retrieved {} employees", employees.size());
        return employees;
    }

    @Override
    public OrderDTO calculateOrderPrice(OrderDTO orderDTO) {
        logger.info("Calculating order price for: {}", orderDTO);

        if (orderDTO == null) {
            logger.warn("OrderDTO is null, returning default order with zero price");
            OrderDTO newOrderDTO = new OrderDTO();
            newOrderDTO.setPrice(BigDecimal.ZERO);
            return newOrderDTO;
        }

        if (orderDTO.getBookItems() == null || orderDTO.getBookItems().isEmpty()) {
            logger.debug("No book items in order, setting price to zero");
            orderDTO.setPrice(BigDecimal.ZERO);
            return orderDTO;
        }

        BigDecimal totalPrice = orderDTO.getBookItems().stream()
                .map(item -> {
                    try {
                        BigDecimal bookPrice = bookPriceService.getBookPrice(item.getBookName().trim());
                        BigDecimal itemTotal = bookPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                        logger.debug("Book: {}, Price: {}, Quantity: {}, Item Total: {}",
                                item.getBookName(), bookPrice, item.getQuantity(), itemTotal);
                        return itemTotal;
                    } catch (Exception e) {
                        logger.warn("Failed to get price for book: {} - {}", item.getBookName(), e.getMessage());
                        return BigDecimal.ZERO;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        orderDTO.setPrice(totalPrice);
        logger.info("Order price calculated: {}", totalPrice);
        return orderDTO;
    }
}
