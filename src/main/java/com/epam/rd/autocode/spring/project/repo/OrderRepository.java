package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByClientEmail(String clientEmail);

    List<Order> findByEmployeeEmail(String employeeEmail);

    List<Order> findByOrderDate(LocalDateTime orderDate);
}
