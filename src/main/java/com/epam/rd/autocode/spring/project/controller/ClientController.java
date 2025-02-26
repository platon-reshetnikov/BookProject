package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @GetMapping
    public List<ClientDTO> getAllClients() {
        return clientService.getAllClients();
    }

    @GetMapping("/{email}")
    public ResponseEntity<ClientDTO> getClientByEmail(@PathVariable String email) {
        ClientDTO client = clientService.getClientByEmail(email);
        return ResponseEntity.ok(client);
    }

    @PostMapping
    public ResponseEntity<ClientDTO> addClient(@RequestBody ClientDTO clientDTO) {
        ClientDTO savedClient = clientService.addClient(clientDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
    }

    @PutMapping("/{email}")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable String email, @RequestBody ClientDTO clientDTO) {
        ClientDTO updatedClient = clientService.updateClientByEmail(email, clientDTO);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteClient(@PathVariable String email) {
        clientService.deleteClientByEmail(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/balance-greater-than/{balance}")
    public List<ClientDTO> getClientsWithBalanceGreaterThan(@PathVariable BigDecimal balance) {
        return clientService.getClientsWithBalanceGreaterThan(balance);
    }
}
