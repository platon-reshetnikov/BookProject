package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    @Autowired
    private BookService bookService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public List<BookDTO> getAllBooks(){
        return bookService.getAllBooks();
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public BookDTO getBookByName(@PathVariable String name){
        return bookService.getBookByName(name);
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public BookDTO addBook(@RequestBody BookDTO bookDTO){
        return bookService.addBook(bookDTO);
    }

    @PutMapping("/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public BookDTO updateBook(@PathVariable String name,@RequestBody BookDTO bookDTO){
        return bookService.updateBookByName(name,bookDTO);
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public void deleteBook(@PathVariable String name){
        bookService.deleteBookByName(name);
    }
}
