package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.MapStruct.ClientMapper;
import com.epam.rd.autocode.spring.project.MapStruct.OrderMapper;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;


    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        List<Order> orders = orderRepository.findByClientEmail(clientEmail);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        List<Order> orders = orderRepository.findByEmployeeEmail(employeeEmail);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO addOrder(OrderDTO order) {
        Order orders = orderMapper.toEntity(order);
        Order savedOrder = orderRepository.save(orders);
        return orderMapper.toDTO(savedOrder);
    }
}
