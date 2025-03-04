package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;

import java.time.LocalDateTime;
import java.util.*;

public interface OrderService {

    List<OrderDTO> getOrdersByClient(String clientEmail);

    List<OrderDTO> getOrdersByEmployee(String employeeEmail);

    OrderDTO addOrder(OrderDTO order);

    List<OrderDTO> getOrdersByOrderDate(LocalDateTime orderDate);

    OrderDTO confirmOrder(String clientEmail, LocalDateTime orderDate, String employeeEmail); // Обновленный метод

    List<OrderDTO> getAllOrders();

    OrderDTO findOrderByClientEmailAndOrderDate(String clientEmail, LocalDateTime orderDate); // Новый метод
}
