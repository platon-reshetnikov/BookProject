package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/client/{clientEmail}")
    public ResponseEntity<?> getOrdersByClient(@PathVariable String clientEmail, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        List<OrderDTO> orders = orderService.getOrdersByClient(clientEmail);
        if (orders.isEmpty()) {
            String message = messageSource.getMessage("orders.not.found", new Object[]{clientEmail}, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/employee/{employeeEmail}")
    public ResponseEntity<?> getOrdersByEmployee(@PathVariable String employeeEmail, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        List<OrderDTO> orders = orderService.getOrdersByEmployee(employeeEmail);
        if (orders.isEmpty()) {
            String message = messageSource.getMessage("orders.not.found", new Object[]{employeeEmail}, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<?> addOrder(@Valid @RequestBody OrderDTO orderDTO, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        OrderDTO savedOrder = orderService.addOrder(orderDTO);
        String message = messageSource.getMessage("order.added", new Object[]{savedOrder.getClientEmail()}, locale);
        return ResponseEntity.status(HttpStatus.CREATED).header("X-Message", message).body(savedOrder);
    }

    @GetMapping("/order-date/{orderDate}")
    public List<OrderDTO> getOrdersByOrderDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime orderDate) {
        return orderService.getOrdersByOrderDate(orderDate);
    }
}