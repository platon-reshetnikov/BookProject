package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.MapStruct.ClientMapper;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    public final ClientMapper clientMapper;


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
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return clientMapper.toDTO(client);
    }

    @Override
    public ClientDTO updateClientByEmail(String email, ClientDTO client) {
        Client existingClient = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        clientMapper.updateEntityFromDTO(client, existingClient);
        Client updatedClient = clientRepository.save(existingClient);
        return clientMapper.toDTO(updatedClient);
    }

    @Override
    public void deleteClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        clientRepository.delete(client);
    }

    @Override
    public ClientDTO addClient(ClientDTO client) {
        Client clients = clientMapper.toEntity(client);
        Client savedClient = clientRepository.save(clients);
        return clientMapper.toDTO(savedClient);
    }
}
