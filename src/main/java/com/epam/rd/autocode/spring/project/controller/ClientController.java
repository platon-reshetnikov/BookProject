package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping
    public List<ClientDTO> getAllClients(){
        return clientService.getAllClients();
    }

    @GetMapping("/{email}")
    public ClientDTO getClientByEmail(@PathVariable String email){
        return clientService.getClientByEmail(email);
    }

    @PostMapping
    public ClientDTO addClient(@RequestBody ClientDTO clientDTO){
        return clientService.addClient(clientDTO);
    }

    @PutMapping("/{email}")
    public ClientDTO updateClient(@PathVariable String email,@RequestBody ClientDTO clientDTO){
        return clientService.updateClientByEmail(email,clientDTO);
    }

    @DeleteMapping("/{email}")
    public void deleteClient(@PathVariable String email){
        clientService.deleteClientByEmail(email);
    }
}
