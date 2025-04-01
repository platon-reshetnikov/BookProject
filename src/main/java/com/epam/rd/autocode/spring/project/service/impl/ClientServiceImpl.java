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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<ClientDTO> getAllClients(Pageable pageable, String search) {
        logger.info("Retrieving clients - Page: {}, Size: {}, Search: {}", pageable.getPageNumber(), pageable.getPageSize(), search);

        Page<Client> clients;
        if (search != null && !search.isEmpty()) {
            logger.debug("Searching clients with email or name containing: {}", search);
            clients = clientRepository.findByEmailContainingOrNameContaining(search, search, pageable);
        } else {
            logger.debug("Fetching all clients");
            clients = clientRepository.findAll(pageable);
        }

        logger.debug("Retrieved {} clients out of {} total", clients.getNumberOfElements(), clients.getTotalElements());
        return clients.map(clientMapper::toDTO);
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        logger.info("Retrieving client by email: {}", email);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Client not found with email: {}", email);
                    return new NotFoundException("Client not found with email: " + email);
                });

        logger.debug("Client retrieved: {}", client);
        return clientMapper.toDTO(client);
    }

    @Override
    public void deleteClientByEmail(String email) {
        logger.info("Deleting client by email: {}", email);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Client not found for deletion with email: {}", email);
                    return new NotFoundException("Client not found with email: " + email);
                });

        clientRepository.delete(client);
        clientBlockedStatus.remove(email);
        clientBaskets.remove(email);
        logger.info("Client deleted successfully: {}", email);
    }

    @Override
    public void blockClient(String email) {
        logger.info("Blocking client: {}", email);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Client not found for blocking with email: {}", email);
                    return new NotFoundException("Client not found with email: " + email);
                });

        clientBlockedStatus.put(email, true);
        logger.info("Client blocked successfully: {}", email);
    }

    @Override
    public void unblockClient(String email) {
        logger.info("Unblocking client: {}", email);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Client not found for unblocking with email: {}", email);
                    return new NotFoundException("Client not found with email: " + email);
                });

        clientBlockedStatus.put(email, false);
        logger.info("Client unblocked successfully: {}", email);
    }

    @Override
    public boolean isClientBlocked(String email) {
        boolean isBlocked = clientBlockedStatus.getOrDefault(email, false);
        logger.debug("Checking if client is blocked - Email: {}, Status: {}", email, isBlocked);
        return isBlocked;
    }

    @Override
    public void addBookToBasket(String clientEmail, String bookName, int quantity) {
        logger.info("Adding book to basket - Client: {}, Book: {}, Quantity: {}", clientEmail, bookName, quantity);

        if (quantity <= 0) {
            logger.warn("Invalid quantity provided: {}", quantity);
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> {
                    logger.warn("Client not found for adding book to basket: {}", clientEmail);
                    return new NotFoundException("Client not found with email: " + clientEmail);
                });

        List<BookItemDTO> basket = clientBaskets.getOrDefault(clientEmail, new ArrayList<>());
        Optional<BookItemDTO> existingItem = basket.stream()
                .filter(item -> item.getBookName().equals(bookName))
                .findFirst();

        if (existingItem.isPresent()) {
            int newQuantity = existingItem.get().getQuantity() + quantity;
            existingItem.get().setQuantity(newQuantity);
            logger.debug("Updated existing book item quantity - Book: {}, New Quantity: {}", bookName, newQuantity);
        } else {
            basket.add(new BookItemDTO(bookName, quantity));
            logger.debug("Added new book item to basket - Book: {}, Quantity: {}", bookName, quantity);
        }

        clientBaskets.put(clientEmail, basket);
        logger.info("Book added to basket successfully - Client: {}, Book: {}", clientEmail, bookName);
    }

    @Override
    public List<BookItemDTO> getBasket(String clientEmail) {
        logger.info("Retrieving basket for client: {}", clientEmail);

        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> {
                    logger.warn("Client not found for retrieving basket: {}", clientEmail);
                    return new NotFoundException("Client not found with email: " + clientEmail);
                });

        List<BookItemDTO> basket = clientBaskets.getOrDefault(clientEmail, new ArrayList<>());
        logger.debug("Basket retrieved with {} items for client: {}", basket.size(), clientEmail);
        return basket;
    }

    @Override
    public void clearBasket(String clientEmail) {
        logger.info("Clearing basket for client: {}", clientEmail);

        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> {
                    logger.warn("Client not found for clearing basket: {}", clientEmail);
                    return new NotFoundException("Client not found with email: " + clientEmail);
                });

        clientBaskets.remove(clientEmail);
        logger.info("Basket cleared successfully for client: {}", clientEmail);
    }
}
