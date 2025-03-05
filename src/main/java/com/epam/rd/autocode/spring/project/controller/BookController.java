package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

@Controller // Use @Controller instead of @RestController for HTML rendering
@RequestMapping("/books")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public ModelAndView getAllBooks() {
        logger.info("User: {}, Roles: {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        List<BookDTO> books = bookService.getAllBooks();
        ModelAndView modelAndView = new ModelAndView("books");
        modelAndView.addObject("books", books);
        return modelAndView;
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public ModelAndView getBookByName(@PathVariable String name) {
        logger.info("User: {}, Roles: {}, Accessing book: {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities(),
                name);
        BookDTO book = bookService.getBookByName(name);
        if (book == null) {
            throw new NotFoundException("Book not found");
        }
        ModelAndView modelAndView = new ModelAndView("book-details");
        modelAndView.addObject("book", book);
        return modelAndView;
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ModelAndView showAddBookForm() {
        logger.info("User: {}, Roles: {}, Accessing add book form",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        ModelAndView modelAndView = new ModelAndView("book-form");
        modelAndView.addObject("book", new BookDTO());
        modelAndView.addObject("ageGroups", AgeGroup.values());
        modelAndView.addObject("languages", Language.values());
        return modelAndView;
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String addBook(@Valid @ModelAttribute("book") BookDTO bookDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        logger.info("User: {}, Roles: {}, Adding book: {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities(),
                bookDTO);
        if (bindingResult.hasErrors()) {
            logger.error("Validation errors: {}", bindingResult.getAllErrors());
            return "book-form";
        }
        bookService.addBook(bookDTO);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("book.added", new Object[]{bookDTO.getName()}, locale);
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/books";
    }

    @GetMapping("/edit/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ModelAndView showEditBookForm(@PathVariable String name) {
        logger.info("User: {}, Roles: {}, Editing book: {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities(),
                name);
        BookDTO book = bookService.getBookByName(name);
        if (book == null) {
            throw new NotFoundException("Book not found");
        }
        ModelAndView modelAndView = new ModelAndView("book-form");
        modelAndView.addObject("book", book);
        modelAndView.addObject("ageGroups", AgeGroup.values());
        modelAndView.addObject("languages", Language.values());
        return modelAndView;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String updateBook(
            @Valid @ModelAttribute("book") BookDTO bookDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("User: {}, Roles: {}, Updating book: {}", username, roles, bookDTO);

        if (bindingResult.hasErrors()) {
            logger.error("Validation errors: {}", bindingResult.getAllErrors());
            return "book-form";
        }

        // Ensure proper date format before updating the book
        if (bookDTO.getPublicationDate() != null) {
            logger.info("Publication Date: {}", bookDTO.getPublicationDate());
        } else {
            logger.warn("Publication Date is null for book: {}", bookDTO.getName());
        }

        bookService.updateBookByName(bookDTO.getName(), bookDTO);

        // Fetch locale and display localized success message
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("book.updated", new Object[]{bookDTO.getName()}, locale);
        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/books";
    }

    @PostMapping("/delete/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String deleteBook(@PathVariable String name, RedirectAttributes redirectAttributes) {
        logger.info("User: {}, Roles: {}, Deleting book: {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities(),
                name);
        try {
            bookService.deleteBookByName(name);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("book.deleted", new Object[]{name}, locale);
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (NotFoundException e) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("book.not.found", new Object[]{name}, locale);
            redirectAttributes.addFlashAttribute("errorMessage", message);
        }
        return "redirect:/books";
    }

    @GetMapping("/genre/{genre}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public ModelAndView getBooksByGenre(@PathVariable String genre) {
        List<BookDTO> books = bookService.getBooksByGenre(genre);
        ModelAndView modelAndView = new ModelAndView("books");
        modelAndView.addObject("books", books);
        return modelAndView;
    }

    @GetMapping("/age-group/{ageGroup}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public ModelAndView getBooksByAgeGroup(@PathVariable AgeGroup ageGroup) {
        List<BookDTO> books = bookService.getBooksByAgeGroup(ageGroup);
        ModelAndView modelAndView = new ModelAndView("books");
        modelAndView.addObject("books", books);
        return modelAndView;
    }

    @GetMapping("/language/{language}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public ModelAndView getBooksByLanguage(@PathVariable Language language) {
        List<BookDTO> books = bookService.getBooksByLanguage(language);
        ModelAndView modelAndView = new ModelAndView("books");
        modelAndView.addObject("books", books);
        return modelAndView;
    }
}