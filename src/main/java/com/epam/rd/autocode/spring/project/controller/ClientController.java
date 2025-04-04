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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public String getAllClients(Model model,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                @RequestParam(name = "size", defaultValue = "10") int size,
                                @RequestParam(name = "sort", defaultValue = "email,asc") String sort,
                                @RequestParam(name = "search", required = false) String search,
                                @RequestParam(name = "lang", required = false) String lang) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Retrieving clients list - User: {}, Roles: {}, Page: {}, Size: {}, Sort: {}, Search: {}",
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

        Page<ClientDTO> clientPage = clientService.getAllClients(pageable, search);
        logger.debug("Retrieved {} clients out of {} total", clientPage.getNumberOfElements(), clientPage.getTotalElements());

        model.addAttribute("clients", clientPage.getContent());
        model.addAttribute("currentPage", clientPage.getNumber());
        model.addAttribute("totalPages", clientPage.getTotalPages());
        model.addAttribute("totalItems", clientPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection.toString().toLowerCase());
        model.addAttribute("search", search);

        Map<String, Boolean> blockedStatus = new HashMap<>();
        for (ClientDTO client : clientPage.getContent()) {
            blockedStatus.put(client.getEmail(), clientService.isClientBlocked(client.getEmail()));
        }
        model.addAttribute("clientBlockedStatus", blockedStatus);

        return "clients";
    }

    @PostMapping("/block/{email}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String blockClient(@PathVariable String email, Model model,
                              @RequestParam(name = "lang", required = false) String lang) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
        logger.info("Blocking client - User: {}, Roles: {}, Client email: {}", username, roles, email);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        try {
            clientService.blockClient(email);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.blocked", new Object[]{email}, locale);
            model.addAttribute("successMessage", message);
            logger.info("Client blocked successfully: {}", email);
        } catch (NotFoundException e) {
            logger.warn("Failed to block client - not found: {}", email);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.found", new Object[]{email}, locale);
            model.addAttribute("errorMessage", message);
        }
        return "redirect:/clients/manage";
    }

    @PostMapping("/unblock/{email}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String unblockClient(@PathVariable String email, Model model,
                                @RequestParam(name = "lang", required = false) String lang) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Unblocking client - User: {}, Roles: {}, Client email: {}", username, roles, email);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        try {
            clientService.unblockClient(email);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.unblocked", new Object[]{email}, locale);
            model.addAttribute("successMessage", message);
            logger.info("Client unblocked successfully: {}", email);
        } catch (NotFoundException e) {
            logger.warn("Failed to unblock client - not found: {}", email);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.found", new Object[]{email}, locale);
            model.addAttribute("errorMessage", message);
        }
        return "redirect:/clients/manage";
    }

    @PostMapping("/basket/add/{bookName}")
    @PreAuthorize("hasRole('CLIENT')")
    public String addBookToBasket(@PathVariable String bookName,
                                  @RequestParam(defaultValue = "1") int quantity,
                                  Model model,
                                  @RequestParam(name = "lang", required = false) String lang) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        logger.info("Adding book to basket - Client: {}, Book: {}, Quantity: {}", clientEmail, bookName, quantity);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        try {
            clientService.addBookToBasket(clientEmail, bookName, quantity);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("basket.added", new Object[]{bookName}, locale);
            model.addAttribute("successMessage", message);
            logger.info("Book added to basket successfully: {}", bookName);
        } catch (NotFoundException e) {
            logger.warn("Failed to add book to basket - client not found: {}", clientEmail);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.found", new Object[]{clientEmail}, locale);
            model.addAttribute("errorMessage", message);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add book to basket: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/books";
    }

    @GetMapping("/basket")
    @PreAuthorize("hasRole('CLIENT')")
    public String viewBasket(Model model, @RequestParam(name = "lang", required = false) String lang) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        logger.info("Viewing basket - Client: {}", clientEmail);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        List<BookItemDTO> basket = clientService.getBasket(clientEmail);
        logger.debug("Basket contains {} items", basket.size());
        model.addAttribute("basket", basket);
        return "basket";
    }

    @PostMapping("/basket/clear")
    @PreAuthorize("hasRole('CLIENT')")
    public String clearBasket(Model model, @RequestParam(name = "lang", required = false) String lang) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        logger.info("Clearing basket - Client: {}", clientEmail);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        try {
            clientService.clearBasket(clientEmail);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("basket.cleared", null, locale);
            model.addAttribute("successMessage", message);
            logger.info("Basket cleared successfully for client: {}", clientEmail);
        } catch (NotFoundException e) {
            logger.warn("Failed to clear basket - client not found: {}", clientEmail);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.found", new Object[]{clientEmail}, locale);
            model.addAttribute("errorMessage", message);
        }
        return "redirect:/clients/basket";
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('CLIENT')")
    public String deleteAccount(HttpServletRequest request, HttpServletResponse response,
                                Model model, @RequestParam(name = "lang", required = false) String lang) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        logger.info("Deleting account - Client: {}", clientEmail);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        try {
            clientService.deleteClientByEmail(clientEmail);
            logger.info("Account deleted successfully: {}", clientEmail);
            new SecurityContextLogoutHandler().logout(request, response,
                    SecurityContextHolder.getContext().getAuthentication());
            return "redirect:/login?deleted";
        } catch (NotFoundException e) {
            logger.warn("Failed to delete account - client not found: {}", clientEmail);
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

        logger.info("Submitting order - Client: {}", clientEmail);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        try {
            clientService.getClientByEmail(clientEmail);
        } catch (NotFoundException e) {
            logger.warn("Client not found for order submission: {}", clientEmail);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.exists", null, locale);
            model.addAttribute("errorMessage", message);
            return "redirect:/login?error";
        }

        List<BookItemDTO> basket = clientService.getBasket(clientEmail);
        if (basket == null || basket.isEmpty()) {
            logger.warn("Attempt to submit empty basket by client: {}", clientEmail);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("basket.empty", null, locale);
            model.addAttribute("errorMessage", message);
            return "basket";
        }

        try {
            List<Employee> employees = employeeRepository.findAll();
            if (employees.isEmpty()) {
                logger.warn("No employees available to process order for client: {}", clientEmail);
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("order.no.employees", null, locale);
                model.addAttribute("errorMessage", message);
                return "basket";
            }
            String employeeEmail = employees.get(new Random().nextInt(employees.size())).getEmail();
            logger.debug("Assigned employee for order: {}", employeeEmail);

            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setClientEmail(clientEmail);
            orderDTO.setEmployeeEmail(employeeEmail);
            orderDTO.setOrderDate(LocalDateTime.now());
            orderDTO.setBookItems(basket);
            orderDTO.setPrice(BigDecimal.ZERO);

            orderDTO = orderService.calculateOrderPrice(orderDTO);
            logger.debug("Calculated order price: {}", orderDTO.getPrice());

            orderService.addOrder(orderDTO);
            clientService.clearBasket(clientEmail);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("order.submitted", new Object[]{employeeEmail}, locale);
            model.addAttribute("successMessage", message);
            logger.info("Order submitted successfully for client: {}, assigned to employee: {}", clientEmail, employeeEmail);
            return "redirect:/clients/basket";
        } catch (Exception e) {
            logger.error("Error submitting order for client: {} - {}", clientEmail, e.getMessage());
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("order.error", new Object[]{e.getMessage()}, locale);
            model.addAttribute("errorMessage", message);
            return "basket";
        }
    }
}
