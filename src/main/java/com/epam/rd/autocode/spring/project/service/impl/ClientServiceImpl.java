package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final OrderRepository orderRepository;

    private final Map<String, Boolean> clientBlockedStatus = new HashMap<>();
    private final Map<String, List<BookItemDTO>> clientBaskets = new HashMap<>();

    @Override
    public Page<ClientDTO> getAllClients(Pageable pageable, String search) {
        Page<Client> clients;
        if (search != null && !search.isEmpty()) {
            clients = clientRepository.findByEmailContainingOrNameContaining(search, search, pageable);
        } else {
            clients = clientRepository.findAll(pageable);
        }
        return clients.map(clientMapper::toDTO);
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        return clientMapper.toDTO(client);
    }

    @Override
    @Transactional
    public void deleteClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        orderRepository.deleteByClientEmail(email);
        clientRepository.delete(client);
        clientBlockedStatus.remove(email);
        clientBaskets.remove(email);
    }

    @Override
    public void blockClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        clientBlockedStatus.put(email, true);
    }

    @Override
    public void unblockClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        clientBlockedStatus.put(email, false);
    }

    @Override
    public boolean isClientBlocked(String email) {
        return clientBlockedStatus.getOrDefault(email, false);
    }

    @Override
    public void addBookToBasket(String clientEmail, String bookName, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + clientEmail));
        List<BookItemDTO> basket = clientBaskets.getOrDefault(clientEmail, new ArrayList<>());
        Optional<BookItemDTO> existingItem = basket.stream()
                .filter(item -> item.getBookName().equals(bookName))
                .findFirst();
        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            basket.add(new BookItemDTO(bookName, quantity));
        }
        clientBaskets.put(clientEmail, basket);
    }

    @Override
    public List<BookItemDTO> getBasket(String clientEmail) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + clientEmail));
        return clientBaskets.getOrDefault(clientEmail, new ArrayList<>());
    }

    @Override
    public void clearBasket(String clientEmail) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + clientEmail));
        clientBaskets.remove(clientEmail);
    }
}
