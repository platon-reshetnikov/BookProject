package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/client/{clientEmail}")
    public ResponseEntity<List<OrderDTO>> getOrdersByClient(@PathVariable String clientEmail) {
        List<OrderDTO> orders = orderService.getOrdersByClient(clientEmail);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/employee/{employeeEmail}")
    public ResponseEntity<List<OrderDTO>> getOrdersByEmployee(@PathVariable String employeeEmail) {
        List<OrderDTO> orders = orderService.getOrdersByEmployee(employeeEmail);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> addOrder(@RequestBody OrderDTO orderDTO) {
        OrderDTO savedOrder = orderService.addOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }
}
