package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private MessageSource messageSource;

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
        logger.info("Employee accessing all orders");
        List<OrderDTO> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @PostMapping("/confirm/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String confirmOrder(@PathVariable Long id, Model model, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        logger.info("Employee confirming order with id: {}", id);
        try {
            String employeeEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            OrderDTO confirmedOrder = orderService.confirmOrder(id, employeeEmail);
            String message = messageSource.getMessage("order.confirmed", new Object[]{confirmedOrder.getClientEmail()}, locale);
            model.addAttribute("successMessage", message);
            return "redirect:/orders";
        } catch (NotFoundException e) {
            String message = messageSource.getMessage("order.not.found", new Object[]{id}, locale);
            model.addAttribute("errorMessage", message);
            return "orders";
        }
    }
}