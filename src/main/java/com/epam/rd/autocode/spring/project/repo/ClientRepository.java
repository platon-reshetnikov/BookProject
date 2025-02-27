package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Repository
public interface ClientRepository extends JpaRepository<Client,Long> {
    Optional<Client> findByEmail(String email);

    List<Client> findByBalanceGreaterThan(BigDecimal balance);
}
