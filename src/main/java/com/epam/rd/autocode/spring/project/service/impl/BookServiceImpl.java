package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.exception.DuplicateResourceException;
import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable, String search) {
        Page<Book> books;
        if (search != null && !search.isEmpty()) {
            books = bookRepository.findByNameContaining(search, pageable);
        } else {
            books = bookRepository.findAll(pageable);
        }
        return books.map(bookMapper::toDTO);
    }

    @Override
    public BookDTO getBookByName(String name) {
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Book not found with name: " + name));
        return bookMapper.toDTO(book);
    }

    @Override
    public BookDTO updateBookByName(String name, @Valid BookDTO bookDTO) {
        Book existingBook = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Book not found with name: " + name));
        bookMapper.updateEntityFromDTO(bookDTO, existingBook);
        Book updatedBook = bookRepository.save(existingBook);
        return bookMapper.toDTO(updatedBook);
    }

    @Override
    public void deleteBookByName(String name) {
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Book not found with name: " + name));
        bookRepository.delete(book);
    }

    @Override
    public BookDTO addBook(@Valid BookDTO bookDTO) {
        if (bookRepository.findByName(bookDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Book with name " + bookDTO.getName() + " already exists");
        }
        Book book = bookMapper.toEntity(bookDTO);
        Book savedBook = bookRepository.save(book);
        return bookMapper.toDTO(savedBook);
    }
}
