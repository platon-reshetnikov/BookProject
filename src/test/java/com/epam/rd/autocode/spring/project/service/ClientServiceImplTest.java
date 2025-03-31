package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;
    private ClientDTO clientDTO;
    private static final String CLIENT_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setEmail(CLIENT_EMAIL);

        clientDTO = new ClientDTO();
        clientDTO.setEmail(CLIENT_EMAIL);
    }

    @Test
    void getAllClients_ReturnsListOfClients() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        List<Client> clients = Collections.singletonList(client);
        Page<Client> clientPage = new PageImpl<>(clients, pageable, clients.size());

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);

        Page<ClientDTO> result = clientService.getAllClients(pageable, null);

        assertEquals(1, result.getContent().size());
        assertEquals(clientDTO, result.getContent().get(0));
        verify(clientRepository, times(1)).findAll(pageable);
        verify(clientMapper, times(1)).toDTO(client);
    }

    @Test
    void getAllClients_EmptyList_ReturnsEmptyList() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Client> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(clientRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<ClientDTO> result = clientService.getAllClients(pageable, null);

        assertTrue(result.getContent().isEmpty());
        verify(clientRepository, times(1)).findAll(pageable);
        verify(clientMapper, never()).toDTO(any());
    }

    @Test
    void getClientByEmail_ClientExists_ReturnsClientDTO() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);

        ClientDTO result = clientService.getClientByEmail(CLIENT_EMAIL);

        assertEquals(clientDTO, result);
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(clientMapper, times(1)).toDTO(client);
    }

    @Test
    void getClientByEmail_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.getClientByEmail(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(clientMapper, never()).toDTO(any());
    }

    @Test
    void deleteClientByEmail_ClientExists_DeletesClient() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));

        clientService.deleteClientByEmail(CLIENT_EMAIL);

        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(clientRepository, times(1)).delete(client);
    }

    @Test
    void deleteClientByEmail_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.deleteClientByEmail(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(clientRepository, never()).delete(any());
    }

    @Test
    void blockClient_ClientExists_BlocksClient() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));

        clientService.blockClient(CLIENT_EMAIL);

        assertTrue(clientService.isClientBlocked(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void blockClient_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.blockClient(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void unblockClient_ClientExists_UnblocksClient() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        clientService.blockClient(CLIENT_EMAIL);

        clientService.unblockClient(CLIENT_EMAIL);

        assertFalse(clientService.isClientBlocked(CLIENT_EMAIL));
        verify(clientRepository, times(2)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void unblockClient_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.unblockClient(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void isClientBlocked_ClientNotBlocked_ReturnsFalse() {
        boolean result = clientService.isClientBlocked(CLIENT_EMAIL);

        assertFalse(result);
    }

    @Test
    void isClientBlocked_ClientBlocked_ReturnsTrue() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        clientService.blockClient(CLIENT_EMAIL);

        boolean result = clientService.isClientBlocked(CLIENT_EMAIL);

        assertTrue(result);
    }

    @Test
    void addBookToBasket_NewBook_AddsToBasket() {
        String bookName = "Test Book";
        int quantity = 2;
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));

        clientService.addBookToBasket(CLIENT_EMAIL, bookName, quantity);

        List<BookItemDTO> basket = clientService.getBasket(CLIENT_EMAIL);
        assertEquals(1, basket.size());
        assertEquals(bookName, basket.get(0).getBookName());
        assertEquals(quantity, basket.get(0).getQuantity());
        verify(clientRepository, times(2)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void addBookToBasket_ExistingBook_IncreasesQuantity() {
        String bookName = "Test Book";
        int initialQuantity = 2;
        int additionalQuantity = 3;
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));

        clientService.addBookToBasket(CLIENT_EMAIL, bookName, initialQuantity);
        clientService.addBookToBasket(CLIENT_EMAIL, bookName, additionalQuantity);

        List<BookItemDTO> basket = clientService.getBasket(CLIENT_EMAIL);
        assertEquals(1, basket.size());
        assertEquals(bookName, basket.get(0).getBookName());
        assertEquals(initialQuantity + additionalQuantity, basket.get(0).getQuantity());
        verify(clientRepository, times(3)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void addBookToBasket_NonPositiveQuantity_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> clientService.addBookToBasket(CLIENT_EMAIL, "Test Book", 0));
        assertThrows(IllegalArgumentException.class, () -> clientService.addBookToBasket(CLIENT_EMAIL, "Test Book", -1));
        verify(clientRepository, never()).findByEmail(anyString());
    }

    @Test
    void addBookToBasket_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.addBookToBasket(CLIENT_EMAIL, "Test Book", 1));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void getBasket_ClientExists_ReturnsBasket() {
        String bookName = "Test Book";
        int quantity = 2;
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        clientService.addBookToBasket(CLIENT_EMAIL, bookName, quantity);

        List<BookItemDTO> basket = clientService.getBasket(CLIENT_EMAIL);

        assertEquals(1, basket.size());
        assertEquals(bookName, basket.get(0).getBookName());
        assertEquals(quantity, basket.get(0).getQuantity());
        verify(clientRepository, times(2)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void getBasket_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.getBasket(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void clearBasket_ClientExists_ClearsBasket() {
        String bookName = "Test Book";
        int quantity = 2;
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        clientService.addBookToBasket(CLIENT_EMAIL, bookName, quantity);

        clientService.clearBasket(CLIENT_EMAIL);

        List<BookItemDTO> basket = clientService.getBasket(CLIENT_EMAIL);
        assertTrue(basket.isEmpty());
        verify(clientRepository, times(3)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void clearBasket_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.clearBasket(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }
}
