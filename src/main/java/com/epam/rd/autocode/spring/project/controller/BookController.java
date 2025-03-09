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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
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
    public ModelAndView getAllBooks(@RequestParam(name = "lang", required = false) String lang) {
        logger.info("User: {}, Roles: {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из /books)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на /books: {}", Locale.getDefault());
        }
        List<BookDTO> books = bookService.getAllBooks();
        ModelAndView modelAndView = new ModelAndView("books");
        modelAndView.addObject("books", books);
        return modelAndView;
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'CUSTOMER')")
    public ModelAndView getBookByName(@PathVariable String name, @RequestParam(name = "lang", required = false) String lang) {
        logger.info("User: {}, Roles: {}, Accessing book: {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities(),
                name);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из /books/{})", lang, name);
        } else {
            logger.info("Использована локаль по умолчанию на /books/{}: {}", name, Locale.getDefault());
        }
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
    public ModelAndView showAddBookForm(@RequestParam(name = "lang", required = false) String lang) {
        logger.info("User: {}, Roles: {}, Accessing add book form",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из /books/add)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на /books/add: {}", Locale.getDefault());
        }
        ModelAndView modelAndView = new ModelAndView("book-form");
        modelAndView.addObject("book", new BookDTO());
        modelAndView.addObject("ageGroups", AgeGroup.values());
        modelAndView.addObject("languages", Language.values());
        return modelAndView;
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String addBook(@Valid @ModelAttribute("book") BookDTO bookDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes,
                          @RequestParam(name = "lang", required = false) String lang) {
        logger.info("User: {}, Roles: {}, Adding book: {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities(),
                bookDTO);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из POST /books/add)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на POST /books/add: {}", Locale.getDefault());
        }
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
    public ModelAndView showEditBookForm(@PathVariable String name, @RequestParam(name = "lang", required = false) String lang) {
        logger.info("User: {}, Roles: {}, Editing book: {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities(),
                name);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из /books/edit/{})", lang, name);
        } else {
            logger.info("Использована локаль по умолчанию на /books/edit/{}: {}", name, Locale.getDefault());
        }
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
            RedirectAttributes redirectAttributes,
            @RequestParam(name = "lang", required = false) String lang) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("User: {}, Roles: {}, Updating book: {}", username, roles, bookDTO);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из POST /books/update)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на POST /books/update: {}", Locale.getDefault());
        }

        if (bindingResult.hasErrors()) {
            logger.error("Validation errors: {}", bindingResult.getAllErrors());
            return "book-form";
        }

        if (bookDTO.getPublicationDate() != null) {
            logger.info("Publication Date: {}", bookDTO.getPublicationDate());
        } else {
            logger.warn("Publication Date is null for book: {}", bookDTO.getName());
        }

        bookService.updateBookByName(bookDTO.getName(), bookDTO);

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("book.updated", new Object[]{bookDTO.getName()}, locale);
        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/books";
    }

    @PostMapping("/delete/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String deleteBook(@PathVariable String name, RedirectAttributes redirectAttributes,
                             @RequestParam(name = "lang", required = false) String lang) {
        logger.info("User: {}, Roles: {}, Deleting book: {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities(),
                name);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из POST /books/delete/{})", lang, name);
        } else {
            logger.info("Использована локаль по умолчанию на POST /books/delete/{}: {}", name, Locale.getDefault());
        }
        try {
            bookService.deleteBookByName(name);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("book.deleted", new Object[]{name},
                    "Book " + name + " has been deleted successfully", locale);
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (NotFoundException e) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("book.not.found", new Object[]{name},
                    "Book not found with name: " + name, locale);
            redirectAttributes.addFlashAttribute("errorMessage", message);
        }
        return "redirect:/books";
    }
}