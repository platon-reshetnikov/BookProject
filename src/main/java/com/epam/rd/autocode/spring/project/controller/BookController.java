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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Locale;

@Controller
@RequestMapping("/books")
public class BookController {
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

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
        Sort sortOrder = Sort.by(sortDirection, sortField);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<BookDTO> bookPage = bookService.getAllBooks(pageable, search);

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
        BookDTO book = bookService.getBookByName(name);
        if (book == null) {
            throw new NotFoundException("Book not found: " + name);
        }
        ModelAndView modelAndView = new ModelAndView("book-details");
        modelAndView.addObject("book", book);
        return modelAndView;
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ModelAndView showAddBookForm(@RequestParam(name = "lang", required = false) String lang) {
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
        if (bindingResult.hasErrors()) {
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
    public ModelAndView showEditBookForm(@PathVariable String name,
                                         @RequestParam(name = "lang", required = false) String lang) {
        BookDTO book = bookService.getBookByName(name);
        if (book == null) {
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
        if (bindingResult.hasErrors()) {
            return "book-form";
        }
        bookService.updateBookByName(bookDTO.getName(), bookDTO);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("book.updated", new Object[]{bookDTO.getName()}, locale);
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/books";
    }

    @PostMapping("/delete/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String deleteBook(@PathVariable String name,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(name = "lang", required = false) String lang) {
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
}