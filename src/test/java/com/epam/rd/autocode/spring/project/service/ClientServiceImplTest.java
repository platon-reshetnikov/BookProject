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
        // Настраиваем тестовые данные
        client = new Client();
        client.setEmail(CLIENT_EMAIL);

        clientDTO = new ClientDTO();
        clientDTO.setEmail(CLIENT_EMAIL);
    }

    // Тесты для getAllClients
    @Test
    void getAllClients_ReturnsListOfClients() {
        // Arrange
        when(clientRepository.findAll()).thenReturn(Arrays.asList(client));
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);

        // Act
        List<ClientDTO> result = clientService.getAllClients();

        // Assert
        assertEquals(1, result.size());
        assertEquals(clientDTO, result.get(0));
        verify(clientRepository, times(1)).findAll();
        verify(clientMapper, times(1)).toDTO(client);
    }

    @Test
    void getAllClients_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(clientRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ClientDTO> result = clientService.getAllClients();

        // Assert
        assertTrue(result.isEmpty());
        verify(clientRepository, times(1)).findAll();
        verify(clientMapper, never()).toDTO(any());
    }

    // Тесты для getClientByEmail
    @Test
    void getClientByEmail_ClientExists_ReturnsClientDTO() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);

        // Act
        ClientDTO result = clientService.getClientByEmail(CLIENT_EMAIL);

        // Assert
        assertEquals(clientDTO, result);
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(clientMapper, times(1)).toDTO(client);
    }

    @Test
    void getClientByEmail_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> clientService.getClientByEmail(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(clientMapper, never()).toDTO(any());
    }

    // Тесты для deleteClientByEmail
    @Test
    void deleteClientByEmail_ClientExists_DeletesClient() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));

        // Act
        clientService.deleteClientByEmail(CLIENT_EMAIL);

        // Assert
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(clientRepository, times(1)).delete(client);
    }

    @Test
    void deleteClientByEmail_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> clientService.deleteClientByEmail(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
        verify(clientRepository, never()).delete(any());
    }

    // Тесты для blockClient
    @Test
    void blockClient_ClientExists_BlocksClient() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));

        // Act
        clientService.blockClient(CLIENT_EMAIL);

        // Assert
        assertTrue(clientService.isClientBlocked(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }

    @Test
    void blockClient_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> clientService.blockClient(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }

    // Тесты для unblockClient
    @Test
    void unblockClient_ClientExists_UnblocksClient() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        clientService.blockClient(CLIENT_EMAIL); // Сначала блокируем

        // Act
        clientService.unblockClient(CLIENT_EMAIL);

        // Assert
        assertFalse(clientService.isClientBlocked(CLIENT_EMAIL));
        verify(clientRepository, times(2)).findByEmail(CLIENT_EMAIL); // Один раз в block, второй в unblock
    }

    @Test
    void unblockClient_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> clientService.unblockClient(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }

    // Тесты для isClientBlocked
    @Test
    void isClientBlocked_ClientNotBlocked_ReturnsFalse() {
        // Act
        boolean result = clientService.isClientBlocked(CLIENT_EMAIL);

        // Assert
        assertFalse(result);
    }

    @Test
    void isClientBlocked_ClientBlocked_ReturnsTrue() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        clientService.blockClient(CLIENT_EMAIL);

        // Act
        boolean result = clientService.isClientBlocked(CLIENT_EMAIL);

        // Assert
        assertTrue(result);
    }

    // Тесты для addBookToBasket
    @Test
    void addBookToBasket_NewBook_AddsToBasket() {
        // Arrange
        String bookName = "Test Book";
        int quantity = 2;
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));

        // Act
        clientService.addBookToBasket(CLIENT_EMAIL, bookName, quantity);

        // Assert
        List<BookItemDTO> basket = clientService.getBasket(CLIENT_EMAIL);
        assertEquals(1, basket.size());
        assertEquals(bookName, basket.get(0).getBookName());
        assertEquals(quantity, basket.get(0).getQuantity());
        verify(clientRepository, times(2)).findByEmail(CLIENT_EMAIL); // Один раз в add, второй в getBasket
    }

    @Test
    void addBookToBasket_ExistingBook_IncreasesQuantity() {
        // Arrange
        String bookName = "Test Book";
        int initialQuantity = 2;
        int additionalQuantity = 3;
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));

        // Добавляем книгу первый раз
        clientService.addBookToBasket(CLIENT_EMAIL, bookName, initialQuantity);
        // Добавляем ту же книгу второй раз
        clientService.addBookToBasket(CLIENT_EMAIL, bookName, additionalQuantity);

        // Assert
        List<BookItemDTO> basket = clientService.getBasket(CLIENT_EMAIL);
        assertEquals(1, basket.size());
        assertEquals(bookName, basket.get(0).getBookName());
        assertEquals(initialQuantity + additionalQuantity, basket.get(0).getQuantity());
        verify(clientRepository, times(3)).findByEmail(CLIENT_EMAIL); // Два раза в add, один в getBasket
    }

    @Test
    void addBookToBasket_NonPositiveQuantity_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> clientService.addBookToBasket(CLIENT_EMAIL, "Test Book", 0));
        assertThrows(IllegalArgumentException.class, () -> clientService.addBookToBasket(CLIENT_EMAIL, "Test Book", -1));
        verify(clientRepository, never()).findByEmail(anyString());
    }

    @Test
    void addBookToBasket_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> clientService.addBookToBasket(CLIENT_EMAIL, "Test Book", 1));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }

    // Тесты для getBasket
    @Test
    void getBasket_ClientExists_ReturnsBasket() {
        // Arrange
        String bookName = "Test Book";
        int quantity = 2;
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        clientService.addBookToBasket(CLIENT_EMAIL, bookName, quantity);

        // Act
        List<BookItemDTO> basket = clientService.getBasket(CLIENT_EMAIL);

        // Assert
        assertEquals(1, basket.size());
        assertEquals(bookName, basket.get(0).getBookName());
        assertEquals(quantity, basket.get(0).getQuantity());
        verify(clientRepository, times(2)).findByEmail(CLIENT_EMAIL); // Один раз в add, второй в getBasket
    }

    @Test
    void getBasket_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> clientService.getBasket(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }

    // Тесты для clearBasket
    @Test
    void clearBasket_ClientExists_ClearsBasket() {
        // Arrange
        String bookName = "Test Book";
        int quantity = 2;
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        clientService.addBookToBasket(CLIENT_EMAIL, bookName, quantity);

        // Act
        clientService.clearBasket(CLIENT_EMAIL);

        // Assert
        List<BookItemDTO> basket = clientService.getBasket(CLIENT_EMAIL);
        assertTrue(basket.isEmpty());
        verify(clientRepository, times(3)).findByEmail(CLIENT_EMAIL); // add, clear, getBasket
    }

    @Test
    void clearBasket_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> clientService.clearBasket(CLIENT_EMAIL));
        verify(clientRepository, times(1)).findByEmail(CLIENT_EMAIL);
    }
}
