package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public List<ClientDTO> getAllClients() {
        return clientService.getAllClients();
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getClientByEmail(@PathVariable String email, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        ClientDTO client = clientService.getClientByEmail(email);
        if (client == null) {
            String message = messageSource.getMessage("client.not.found", new Object[]{email}, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        return ResponseEntity.ok(client);
    }

    @PostMapping
    public ResponseEntity<?> addClient(@Valid @RequestBody ClientDTO clientDTO, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        ClientDTO savedClient = clientService.addClient(clientDTO);
        String message = messageSource.getMessage("client.added", new Object[]{savedClient.getEmail()}, locale);
        return ResponseEntity.status(HttpStatus.CREATED).header("X-Message", message).body(savedClient);
    }

    @PutMapping("/{email}")
    public ResponseEntity<?> updateClient(@PathVariable String email, @Valid @RequestBody ClientDTO clientDTO, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        ClientDTO updatedClient = clientService.updateClientByEmail(email, clientDTO);
        if (updatedClient == null) {
            String message = messageSource.getMessage("client.not.found", new Object[]{email}, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        String message = messageSource.getMessage("client.updated", new Object[]{updatedClient.getEmail()}, locale);
        return ResponseEntity.ok().header("X-Message", message).body(updatedClient);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteClient(@PathVariable String email, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        try {
            clientService.deleteClientByEmail(email);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            String message = messageSource.getMessage("client.not.found", new Object[]{email}, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @GetMapping("/balance-greater-than/{balance}")
    public List<ClientDTO> getClientsWithBalanceGreaterThan(@PathVariable BigDecimal balance) {
        return clientService.getClientsWithBalanceGreaterThan(balance);
    }
}
