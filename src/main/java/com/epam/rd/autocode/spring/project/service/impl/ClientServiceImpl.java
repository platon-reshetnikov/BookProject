package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    private final Map<String, Boolean> clientBlockedStatus = new HashMap<>();
    private final Map<String, List<BookItemDTO>> clientBaskets = new HashMap<>();

    @Override
    public List<ClientDTO> getAllClients() {
        List<Client> clients = clientRepository.findAll();
        return clients.stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        return clientMapper.toDTO(client);
    }

    @Override
    public void deleteClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        clientRepository.delete(client);
    }

    @Override
    public void blockClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        clientBlockedStatus.put(email, true);
        logger.info("Client blocked: {}", email);
    }

    @Override
    public void unblockClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        clientBlockedStatus.put(email, false);
        logger.info("Client unblocked: {}", email);
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
        logger.info("Book {} added to basket for client: {}", bookName, clientEmail);
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
        logger.info("Basket cleared for client: {}", clientEmail);
    }
}
