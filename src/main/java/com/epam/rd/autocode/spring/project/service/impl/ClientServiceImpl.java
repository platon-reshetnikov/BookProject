package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.MapStruct.ClientMapper;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    // Временное хранилище статуса блокировки
    private final Map<String, Boolean> clientBlockedStatus = new HashMap<>();

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
    public ClientDTO updateClientByEmail(String email, @Valid ClientDTO clientDTO) {
        Client existingClient = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        clientMapper.updateEntityFromDTO(clientDTO, existingClient);
        Client updatedClient = clientRepository.save(existingClient);
        return clientMapper.toDTO(updatedClient);
    }

    @Override
    public void deleteClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        clientRepository.delete(client);
    }

    @Override
    public ClientDTO addClient(@Valid ClientDTO clientDTO) {
        if (clientRepository.findByEmail(clientDTO.getEmail()).isPresent()) {
            throw new AlreadyExistException("Client already exists with email: " + clientDTO.getEmail());
        }
        Client client = clientMapper.toEntity(clientDTO);
        Client savedClient = clientRepository.save(client);
        return clientMapper.toDTO(savedClient);
    }

    @Override
    public List<ClientDTO> getClientsWithBalanceGreaterThan(BigDecimal balance) {
        List<Client> clients = clientRepository.findByBalanceGreaterThan(balance);
        return clients.stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
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
}
