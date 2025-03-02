package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Locale;

@Controller // Use @Controller instead of @RestController for HTML rendering
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public ModelAndView getAllBooks() {
        List<BookDTO> books = bookService.getAllBooks();
        ModelAndView modelAndView = new ModelAndView("books"); // Name of the Thymeleaf template
        modelAndView.addObject("books", books);
        return modelAndView;
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    //public ModelAndView getBookByName(@PathVariable String name, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
    public ModelAndView getBookByName(@PathVariable String name) {
        BookDTO book = bookService.getBookByName(name);
        if (book == null) {
            throw new NotFoundException("Book not found");
        }
        ModelAndView modelAndView = new ModelAndView("book-details"); // Name of the Thymeleaf template for book details
        modelAndView.addObject("book", book);
        return modelAndView;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> addBook(@Valid @RequestBody BookDTO bookDTO, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        BookDTO savedBook = bookService.addBook(bookDTO);
        String message = messageSource.getMessage("book.added", new Object[]{savedBook.getName()}, locale);
        return ResponseEntity.status(HttpStatus.CREATED).header("X-Message", message).body(savedBook);
    }

    @PutMapping("/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> updateBook(@PathVariable String name, @Valid @RequestBody BookDTO bookDTO, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        BookDTO updatedBook = bookService.updateBookByName(name, bookDTO);
        if (updatedBook == null) {
            String message = messageSource.getMessage("book.not.found", new Object[]{name}, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        String message = messageSource.getMessage("book.updated", new Object[]{updatedBook.getName()}, locale);
        return ResponseEntity.ok().header("X-Message", message).body(updatedBook);
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> deleteBook(@PathVariable String name, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        try {
            bookService.deleteBookByName(name);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            String message = messageSource.getMessage("book.not.found", new Object[]{name}, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @GetMapping("/genre/{genre}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public List<BookDTO> getBooksByGenre(@PathVariable String genre) {
        return bookService.getBooksByGenre(genre);
    }

    @GetMapping("/age-group/{ageGroup}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public List<BookDTO> getBooksByAgeGroup(@PathVariable AgeGroup ageGroup) {
        return bookService.getBooksByAgeGroup(ageGroup);
    }

    @GetMapping("/language/{language}")
    public List<BookDTO> getBooksByLanguage(@PathVariable Language language) {
        return bookService.getBooksByLanguage(language);
    }
}