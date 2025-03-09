package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private ClientService clientService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/manage")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getAllClients(Model model, @RequestParam(name = "lang", required = false) String lang) {
        logger.info("Employee accessing all clients");
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из /clients/manage)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на /clients/manage: {}", Locale.getDefault());
        }
        List<ClientDTO> clients = clientService.getAllClients();
        model.addAttribute("clients", clients);
        Map<String, Boolean> blockedStatus = new HashMap<>();
        for (ClientDTO client : clients) {
            blockedStatus.put(client.getEmail(), clientService.isClientBlocked(client.getEmail()));
        }
        model.addAttribute("clientBlockedStatus", blockedStatus);
        return "clients";
    }

    @PostMapping("/block/{email}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String blockClient(@PathVariable String email, Model model, @RequestParam(name = "lang", required = false) String lang) {
        logger.info("Employee blocking client: {}", email);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из POST /clients/block/{})", lang, email);
        } else {
            logger.info("Использована локаль по умолчанию на POST /clients/block/{}: {}", email, Locale.getDefault());
        }
        try {
            clientService.blockClient(email);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.blocked", new Object[]{email}, locale);
            model.addAttribute("successMessage", message);
        } catch (NotFoundException e) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.found", new Object[]{email}, locale);
            model.addAttribute("errorMessage", message);
        }
        return "redirect:/clients/manage";
    }

    @PostMapping("/unblock/{email}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String unblockClient(@PathVariable String email, Model model, @RequestParam(name = "lang", required = false) String lang) {
        logger.info("Employee unblocking client: {}", email);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из POST /clients/unblock/{})", lang, email);
        } else {
            logger.info("Использована локаль по умолчанию на POST /clients/unblock/{}: {}", email, Locale.getDefault());
        }
        try {
            clientService.unblockClient(email);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.unblocked", new Object[]{email}, locale);
            model.addAttribute("successMessage", message);
        } catch (NotFoundException e) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.found", new Object[]{email}, locale);
            model.addAttribute("errorMessage", message);
        }
        return "redirect:/clients/manage";
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getClientList(Model model, @RequestParam(name = "lang", required = false) String lang) {
        logger.info("Employee accessing client list");
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из /clients/list)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на /clients/list: {}", Locale.getDefault());
        }
        List<ClientDTO> clients = clientService.getAllClients();
        model.addAttribute("clients", clients);
        return "client-list";
    }

    @PostMapping("/basket/add/{bookName}")
    @PreAuthorize("hasRole('CLIENT')")
    public String addBookToBasket(@PathVariable String bookName, @RequestParam(defaultValue = "1") int quantity, Model model, @RequestParam(name = "lang", required = false) String lang) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Client {} adding book {} to basket", clientEmail, bookName);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из POST /clients/basket/add/{})", lang, bookName);
        } else {
            logger.info("Использована локаль по умолчанию на POST /clients/basket/add/{}: {}", bookName, Locale.getDefault());
        }
        try {
            clientService.addBookToBasket(clientEmail, bookName, quantity);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("basket.added", new Object[]{bookName}, locale);
            model.addAttribute("successMessage", message);
        } catch (NotFoundException e) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.found", new Object[]{clientEmail}, locale);
            model.addAttribute("errorMessage", message);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/books";
    }

    @GetMapping("/basket")
    @PreAuthorize("hasRole('CLIENT')")
    public String viewBasket(Model model, @RequestParam(name = "lang", required = false) String lang) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Client {} viewing basket", clientEmail);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из /clients/basket)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на /clients/basket: {}", Locale.getDefault());
        }
        List<BookItemDTO> basket = clientService.getBasket(clientEmail);
        model.addAttribute("basket", basket);
        return "basket";
    }

    @PostMapping("/basket/clear")
    @PreAuthorize("hasRole('CLIENT')")
    public String clearBasket(Model model, @RequestParam(name = "lang", required = false) String lang) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Client {} clearing their basket", clientEmail);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из POST /clients/basket/clear)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на POST /clients/basket/clear: {}", Locale.getDefault());
        }
        try {
            clientService.clearBasket(clientEmail);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("basket.cleared", null, locale);
            model.addAttribute("successMessage", message);
        } catch (NotFoundException e) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.found", new Object[]{clientEmail}, locale);
            model.addAttribute("errorMessage", message);
        }
        return "redirect:/clients/basket";
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('CLIENT')")
    public String deleteAccount(HttpServletRequest request, HttpServletResponse response, Model model, @RequestParam(name = "lang", required = false) String lang) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Client {} deleting their account", clientEmail);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из POST /clients/delete)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на POST /clients/delete: {}", Locale.getDefault());
        }
        try {
            clientService.deleteClientByEmail(clientEmail);
            new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
            return "redirect:/login?deleted";
        } catch (NotFoundException e) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.found", new Object[]{clientEmail}, locale);
            model.addAttribute("errorMessage", message);
            return "profile";
        }
    }

    @PostMapping("/basket/submit")
    @PreAuthorize("hasRole('CLIENT')")
    public String submitOrder(Model model, @RequestParam(name = "lang", required = false) String lang) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из POST /clients/basket/submit)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на POST /clients/basket/submit: {}", Locale.getDefault());
        }

        try {
            clientService.getClientByEmail(clientEmail);
        } catch (NotFoundException e) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.exists", null, locale);
            model.addAttribute("errorMessage", message);
            return "redirect:/login?error";
        }

        List<BookItemDTO> basket = clientService.getBasket(clientEmail);
        if (basket == null || basket.isEmpty()) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("basket.empty", null, locale);
            model.addAttribute("errorMessage", message);
            return "basket";
        }

        try {
            List<Employee> employees = employeeRepository.findAll();
            if (employees.isEmpty()) {
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("order.no.employees", null, locale);
                model.addAttribute("errorMessage", message);
                return "basket";
            }
            String employeeEmail = employees.get(new Random().nextInt(employees.size())).getEmail();

            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setClientEmail(clientEmail);
            orderDTO.setEmployeeEmail(employeeEmail);
            orderDTO.setOrderDate(LocalDateTime.now());
            orderDTO.setBookItems(basket);
            orderDTO.setPrice(BigDecimal.ZERO);

            orderDTO = orderService.calculateOrderPrice(orderDTO);

            orderService.addOrder(orderDTO);
            clientService.clearBasket(clientEmail);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("order.submitted", new Object[]{employeeEmail}, locale);
            model.addAttribute("successMessage", message);
            return "redirect:/clients/basket";
        } catch (Exception e) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("order.error", new Object[]{e.getMessage()}, locale);
            model.addAttribute("errorMessage", message);
            return "basket";
        }
    }
}
