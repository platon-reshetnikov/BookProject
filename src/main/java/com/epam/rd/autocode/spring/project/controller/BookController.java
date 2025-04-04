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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Locale;

@Controller
@RequestMapping("/books")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    @Autowired
    private BookService bookService;
    @Autowired
    private MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public ModelAndView getAllBooks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "name,asc") String sort,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "lang", required = false) String lang) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Retrieving books list - User: {}, Roles: {}, Page: {}, Size: {}, Sort: {}, Search: {}",
                username, roles, page, size, sort, search);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
        Sort sortOrder = Sort.by(sortDirection, sortField);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<BookDTO> bookPage = bookService.getAllBooks(pageable, search);
        logger.debug("Retrieved {} books out of {} total", bookPage.getNumberOfElements(), bookPage.getTotalElements());

        ModelAndView modelAndView = new ModelAndView("books");
        modelAndView.addObject("books", bookPage.getContent());
        modelAndView.addObject("currentPage", bookPage.getNumber());
        modelAndView.addObject("totalPages", bookPage.getTotalPages());
        modelAndView.addObject("totalItems", bookPage.getTotalElements());
        modelAndView.addObject("pageSize", size);
        modelAndView.addObject("sortField", sortField);
        modelAndView.addObject("sortDirection", sortDirection.toString().toLowerCase());
        modelAndView.addObject("search", search);

        return modelAndView;
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public ModelAndView getBookByName(@PathVariable String name,
                                      @RequestParam(name = "lang", required = false) String lang) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Retrieving book - User: {}, Roles: {}, Book name: {}", username, roles, name);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        BookDTO book = bookService.getBookByName(name);
        if (book == null) {
            logger.warn("Book not found: {}", name);
            throw new NotFoundException("Book not found: " + name);
        }
        logger.debug("Book retrieved successfully: {}", book);
        ModelAndView modelAndView = new ModelAndView("book-details");
        modelAndView.addObject("book", book);
        return modelAndView;
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ModelAndView showAddBookForm(@RequestParam(name = "lang", required = false) String lang) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Showing add book form - User: {}, Roles: {}", username, roles);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        ModelAndView modelAndView = new ModelAndView("book-form");
        modelAndView.addObject("book", new BookDTO());
        modelAndView.addObject("ageGroups", AgeGroup.values());
        modelAndView.addObject("languages", Language.values());
        return modelAndView;
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String addBook(@Valid @ModelAttribute("book") BookDTO bookDTO,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          @RequestParam(name = "lang", required = false) String lang) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Adding new book - User: {}, Roles: {}, Book: {}", username, roles, bookDTO);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors while adding book: {}", bindingResult.getAllErrors());
            return "book-form";
        }

        bookService.addBook(bookDTO);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("book.added", new Object[]{bookDTO.getName()}, locale);
        redirectAttributes.addFlashAttribute("successMessage", message);
        logger.info("Book added successfully: {}", bookDTO.getName());

        return "redirect:/books";
    }

    @GetMapping("/edit/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ModelAndView showEditBookForm(@PathVariable String name,
                                         @RequestParam(name = "lang", required = false) String lang) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Showing edit book form - User: {}, Roles: {}, Book name: {}", username, roles, name);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        BookDTO book = bookService.getBookByName(name);
        if (book == null) {
            logger.warn("Book not found for editing: {}", name);
            throw new NotFoundException("Book not found: " + name);
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
            RedirectAttributes redirectAttributes,
            @RequestParam(name = "lang", required = false) String lang) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Updating book - User: {}, Roles: {}, Book: {}", username, roles, bookDTO);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors while updating book: {}", bindingResult.getAllErrors());
            return "book-form";
        }

        if (bookDTO.getPublicationDate() != null) {
            logger.debug("Publication Date: {}", bookDTO.getPublicationDate());
        } else {
            logger.warn("Publication Date is null for book: {}", bookDTO.getName());
        }

        bookService.updateBookByName(bookDTO.getName(), bookDTO);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("book.updated", new Object[]{bookDTO.getName()}, locale);
        redirectAttributes.addFlashAttribute("successMessage", message);
        logger.info("Book updated successfully: {}", bookDTO.getName());

        return "redirect:/books";
    }

    @PostMapping("/delete/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String deleteBook(@PathVariable String name,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(name = "lang", required = false) String lang) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Deleting book - User: {}, Roles: {}, Book name: {}", username, roles, name);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        try {
            bookService.deleteBookByName(name);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("book.deleted", new Object[]{name}, locale);
            redirectAttributes.addFlashAttribute("successMessage", message);
            logger.info("Book deleted successfully: {}", name);
        } catch (NotFoundException e) {
            logger.warn("Failed to delete book - not found: {}", name);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("book.not.found", new Object[]{name}, locale);
            redirectAttributes.addFlashAttribute("errorMessage", message);
        }

        return "redirect:/books";
    }
}