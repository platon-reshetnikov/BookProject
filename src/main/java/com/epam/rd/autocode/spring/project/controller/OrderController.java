package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.service.BookPriceService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private BookPriceService bookPriceService;

    @GetMapping("/client/{clientEmail}")
    public String getOrdersByClient(@PathVariable String clientEmail, Model model, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        logger.info("Fetching orders for client: {}", clientEmail);
        try {
            List<OrderDTO> orders = orderService.getOrdersByClient(clientEmail);
            model.addAttribute("orders", orders);
            model.addAttribute("clientEmail", clientEmail);
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "orders";
    }

    @GetMapping("/employee/{employeeEmail}")
    public String getOrdersByEmployee(@PathVariable String employeeEmail, Model model, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        logger.info("Fetching orders for employee: {}", employeeEmail);
        try {
            List<OrderDTO> orders = orderService.getOrdersByEmployee(employeeEmail);
            model.addAttribute("orders", orders);
            model.addAttribute("employeeEmail", employeeEmail);
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "employee-orders";
    }

    @PostMapping
    public String addOrder(@Valid @ModelAttribute("order") OrderDTO orderDTO, Model model, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        logger.info("Adding order: {}", orderDTO);
        try {
            OrderDTO savedOrder = orderService.addOrder(orderDTO);
            String message = messageSource.getMessage("order.added", new Object[]{savedOrder.getClientEmail()}, locale);
            model.addAttribute("successMessage", message);
            return "redirect:/orders/client/" + savedOrder.getClientEmail();
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "add-order";
        }
    }

    @GetMapping("/order-date/{orderDate}")
    public String getOrdersByOrderDate(@PathVariable String orderDate, Model model) {
        logger.info("Fetching orders by date: {}", orderDate);
        LocalDateTime parsedDate = LocalDateTime.parse(orderDate, DateTimeFormatter.ISO_DATE_TIME);
        List<OrderDTO> orders = orderService.getOrdersByOrderDate(parsedDate);
        model.addAttribute("orders", orders);
        return "orders-by-date";
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getAllOrders(Model model) {
        String employeeEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<OrderDTO> orders = orderService.getAllOrders();

        List<OrderDTO> ordersWithPrices = orders.stream()
                .peek(order -> {
                    BigDecimal totalPrice = BigDecimal.ZERO;
                    if (order.getBookItems() != null && !order.getBookItems().isEmpty()) {
                        totalPrice = order.getBookItems().stream()
                                .map(item -> {
                                    try {
                                        BigDecimal bookPrice = bookPriceService.getBookPrice(item.getBookName().trim());
                                        return bookPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                                    } catch (Exception e) {
                                        return BigDecimal.ZERO;
                                    }
                                })
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        order.setPrice(totalPrice);
                    }
                })
                .collect(Collectors.toList());

        List<Employee> employees = orderService.getAllEmployees();
        model.addAttribute("orders", ordersWithPrices); // Передача заказов в модель
        model.addAttribute("employees", employees); // Передача списка сотрудников в модель
        return "orders";
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String confirmOrder(@RequestParam("clientEmail") String clientEmail,
                               @RequestParam("orderDate") LocalDateTime orderDate,
                               @RequestParam("employeeEmail") String employeeEmail,
                               Model model) {
        logger.info("Employee {} confirming order for client {} at {}", employeeEmail, clientEmail, orderDate);
        try {
            orderService.confirmOrder(clientEmail, orderDate, employeeEmail);
            model.addAttribute("successMessage", "Order confirmed successfully");
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders";
    }
}