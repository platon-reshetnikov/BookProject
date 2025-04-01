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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable, String search) {
        logger.info("Retrieving books - Page: {}, Size: {}, Search: {}", pageable.getPageNumber(), pageable.getPageSize(), search);

        Page<Book> books;
        if (search != null && !search.isEmpty()) {
            logger.debug("Searching books with name containing: {}", search);
            books = bookRepository.findByNameContaining(search, pageable);
        } else {
            logger.debug("Fetching all books");
            books = bookRepository.findAll(pageable);
        }

        logger.debug("Retrieved {} books out of {} total", books.getNumberOfElements(), books.getTotalElements());
        return books.map(bookMapper::toDTO);
    }

    @Override
    public BookDTO getBookByName(String name) {
        logger.info("Retrieving book by name: {}", name);

        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> {
                    logger.warn("Book not found with name: {}", name);
                    return new NotFoundException("Book not found with name: " + name);
                });

        logger.debug("Book retrieved: {}", book);
        return bookMapper.toDTO(book);
    }

    @Override
    public BookDTO updateBookByName(String name, @Valid BookDTO bookDTO) {
        logger.info("Updating book - Name: {}, BookDTO: {}", name, bookDTO);

        Book existingBook = bookRepository.findByName(name)
                .orElseThrow(() -> {
                    logger.warn("Book not found for update with name: {}", name);
                    return new NotFoundException("Book not found with name: " + name);
                });

        logger.debug("Existing book before update: {}", existingBook);
        bookMapper.updateEntityFromDTO(bookDTO, existingBook);
        Book updatedBook = bookRepository.save(existingBook);

        logger.info("Book updated successfully: {}", updatedBook.getName());
        logger.debug("Updated book details: {}", updatedBook);
        return bookMapper.toDTO(updatedBook);
    }

    @Override
    public void deleteBookByName(String name) {
        logger.info("Deleting book by name: {}", name);

        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> {
                    logger.warn("Book not found for deletion with name: {}", name);
                    return new NotFoundException("Book not found with name: " + name);
                });

        bookRepository.delete(book);
        logger.info("Book deleted successfully: {}", name);
    }

    @Override
    public BookDTO addBook(@Valid BookDTO bookDTO) {
        logger.info("Adding new book: {}", bookDTO);

        if (bookRepository.findByName(bookDTO.getName()).isPresent()) {
            logger.warn("Duplicate book detected: {}", bookDTO.getName());
            throw new DuplicateResourceException("Book with name " + bookDTO.getName() + " already exists");
        }

        Book book = bookMapper.toEntity(bookDTO);
        Book savedBook = bookRepository.save(book);

        logger.info("Book added successfully: {}", savedBook.getName());
        logger.debug("Saved book details: {}", savedBook);
        return bookMapper.toDTO(savedBook);
    }
}
