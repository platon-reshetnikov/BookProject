package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ClientService {
    Page<ClientDTO> getAllClients(Pageable pageable, String search);

    ClientDTO getClientByEmail(String email);

    void deleteClientByEmail(String email);

    void blockClient(String email);

    void unblockClient(String email);

    boolean isClientBlocked(String email);

    void addBookToBasket(String clientEmail, String bookName, int quantity);

    List<BookItemDTO> getBasket(String clientEmail);

    void clearBasket(String clientEmail);

}
