package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllClientsRest() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getClientByEmailRest(@PathVariable String email) {
        ClientDTO client = clientService.getClientByEmail(email);
        if (client == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client not found with email: " + email);
        }
        return ResponseEntity.ok(client);
    }

    @PostMapping
    public ResponseEntity<?> addClientRest(@Valid @RequestBody ClientDTO clientDTO) {
        ClientDTO savedClient = clientService.addClient(clientDTO);
        return ResponseEntity.status(HttpStatus.CREATED).header("X-Message", "Client added: " + savedClient.getEmail()).body(savedClient);
    }

    @PutMapping("/{email}")
    public ResponseEntity<?> updateClientRest(@PathVariable String email, @Valid @RequestBody ClientDTO clientDTO) {
        ClientDTO updatedClient = clientService.updateClientByEmail(email, clientDTO);
        if (updatedClient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client not found with email: " + email);
        }
        return ResponseEntity.ok().header("X-Message", "Client updated: " + updatedClient.getEmail()).body(updatedClient);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteClientRest(@PathVariable String email) {
        try {
            clientService.deleteClientByEmail(email);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client not found with email: " + email);
        }
    }

    @GetMapping("/balance-greater-than/{balance}")
    public ResponseEntity<List<ClientDTO>> getClientsWithBalanceGreaterThanRest(@PathVariable BigDecimal balance) {
        return ResponseEntity.ok(clientService.getClientsWithBalanceGreaterThan(balance));
    }

    @GetMapping("/manage")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getAllClients(Model model) {
        logger.info("Employee accessing all clients");
        List<ClientDTO> clients = clientService.getAllClients();
        model.addAttribute("clients", clients);
        Map<String, Boolean> blockedStatus = new HashMap<>();
        for (ClientDTO client : clients) {
            blockedStatus.put(client.getEmail(), clientService.isClientBlocked(client.getEmail()));
        }
        model.addAttribute("clientBlockedStatus", blockedStatus);
        return "clients";
    }

    @PostMapping("/block/{email}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String blockClient(@PathVariable String email, Model model) {
        logger.info("Employee blocking client: {}", email);
        try {
            clientService.blockClient(email);
            model.addAttribute("successMessage", "Client " + email + " has been blocked");
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", "Client not found with email: " + email);
        }
        return "redirect:/clients/manage";
    }

    @PostMapping("/unblock/{email}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String unblockClient(@PathVariable String email, Model model) {
        logger.info("Employee unblocking client: {}", email);
        try {
            clientService.unblockClient(email);
            model.addAttribute("successMessage", "Client " + email + " has been unblocked");
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", "Client not found with email: " + email);
        }
        return "redirect:/clients/manage";
    }
}
