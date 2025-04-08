package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByClientEmail(String clientEmail);
    List<Order> findByEmployeeEmail(String employeeEmail);
    Optional<Order> findByClientEmailAndOrderDate(String clientEmail, LocalDateTime orderDate);
    void deleteByClientEmail(String clientEmail);
}
