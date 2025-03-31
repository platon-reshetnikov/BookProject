package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.DuplicateResourceException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
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
public class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private BookDTO bookDTO;
    private Book book;

    @BeforeEach
    void setUp(){
        book = new Book();
        book.setName("Test Book");

        bookDTO = new BookDTO();
        bookDTO.setName("Test Book");
    }

    @Test
    void getAllBooks_ReturnsListOfBooks() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        List<Book> books = Collections.singletonList(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDTO(book)).thenReturn(bookDTO);

        Page<BookDTO> result = bookService.getAllBooks(pageable, null);

        assertEquals(1, result.getContent().size());
        assertEquals(bookDTO, result.getContent().get(0));
        verify(bookRepository, times(1)).findAll(pageable);
        verify(bookMapper, times(1)).toDTO(book);
    }

    @Test
    void getAllBooks_EmptyList_ReturnsEmptyList() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<Book> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(bookRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<BookDTO> result = bookService.getAllBooks(pageable, null);

        assertTrue(result.getContent().isEmpty());
        verify(bookRepository, times(1)).findAll(pageable);
        verify(bookMapper, never()).toDTO(any());
    }

    @Test
    void getBookByName_BookExists_ReturnsBookDTO(){
        String name = "Test Book";

        when(bookRepository.findByName(name)).thenReturn(Optional.of(book));
        when(bookMapper.toDTO(book)).thenReturn(bookDTO);

        BookDTO result = bookService.getBookByName(name);

        assertEquals(bookDTO, result);
        verify(bookRepository, times(1)).findByName(name);
        verify(bookMapper, times(1)).toDTO(book);
    }

    @Test
    void getBookByName_BookNotFound_ThrowsNotFoundException(){
        String name = "Nonexistent Book";
        when(bookRepository.findByName(name)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.getBookByName(name));
        verify(bookRepository, times(1)).findByName(name);
        verify(bookMapper, never()).toDTO(any());
    }

    @Test
    void updateBookByName_BookExists_ReturnsUpdatedBookDTO(){
        String name = "Test Book";
        Book updatedBook = new Book();
        updatedBook.setName("Test Book Updated");

        when(bookRepository.findByName(name)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(updatedBook);
        when(bookMapper.toDTO(updatedBook)).thenReturn(bookDTO);

        BookDTO result = bookService.updateBookByName(name, bookDTO);

        assertEquals(bookDTO, result);
        verify(bookRepository, times(1)).findByName(name);
        verify(bookMapper, times(1)).updateEntityFromDTO(bookDTO, book);
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toDTO(updatedBook);
    }

    @Test
    void updateBookByName_BookNotFound_ThrowsNotFoundException(){
        String name = "Nonexistent Book";
        when(bookRepository.findByName(name)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.updateBookByName(name, bookDTO));
        verify(bookRepository, times(1)).findByName(name);
        verify(bookMapper, never()).updateEntityFromDTO(any(), any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void deleteBookByName_BookExists_DeletesBook(){
        String name = "Test Book";
        when(bookRepository.findByName(name)).thenReturn(Optional.of(book));

        bookService.deleteBookByName(name);
        verify(bookRepository, times(1)).findByName(name);
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void deleteBookByName_BookNotFound_ThrowsNotFoundException(){
        String name = "Nonexistent Book";
        when(bookRepository.findByName(name)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.deleteBookByName(name));
        verify(bookRepository, times(1)).findByName(name);
        verify(bookRepository, never()).delete(any());
    }


    @Test
    void addBook_NewBook_ReturnsSavedBookDTO(){
        when(bookRepository.findByName(bookDTO.getName())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(bookDTO)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDTO(book)).thenReturn(bookDTO);

        BookDTO result = bookService.addBook(bookDTO);

        assertEquals(bookDTO, result);
        verify(bookRepository, times(1)).findByName(bookDTO.getName());
        verify(bookMapper, times(1)).toEntity(bookDTO);
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toDTO(book);
    }

    @Test
    void addBook_DuplicateBook_ThrowsDuplicateResourceException(){
        when(bookRepository.findByName(bookDTO.getName())).thenReturn(Optional.of(book));

        assertThrows(DuplicateResourceException.class, () -> bookService.addBook(bookDTO));
        verify(bookRepository, times(1)).findByName(bookDTO.getName());
        verify(bookMapper, never()).toEntity(any());
        verify(bookRepository, never()).save(any());
    }
}
