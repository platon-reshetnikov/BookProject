package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.service.BookPriceService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private OrderService orderService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private BookPriceService bookPriceService;

    @GetMapping("/client/{clientEmail}")
    public String getOrdersByClient(@PathVariable String clientEmail, Model model,
                                    @RequestParam(name = "lang", required = false) String lang) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Retrieving orders - User: {}, Roles: {}, Client email: {}", username, roles, clientEmail);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        try {
            List<OrderDTO> orders = orderService.getOrdersByClient(clientEmail);
            logger.debug("Retrieved {} orders for client: {}", orders.size(), clientEmail);
            model.addAttribute("orders", orders);
            model.addAttribute("clientEmail", clientEmail);
        } catch (NotFoundException e) {
            logger.warn("Failed to retrieve orders - client not found: {}", clientEmail);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("client.not.found", new Object[]{clientEmail}, locale);
            model.addAttribute("errorMessage", message);
        }
        return "orders";
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getAllOrders(Model model, @RequestParam(name = "lang", required = false) String lang) {
        String employeeEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Retrieving all orders - Employee: {}, Roles: {}", employeeEmail, roles);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        List<OrderDTO> orders = orderService.getAllOrders();
        logger.debug("Retrieved {} orders total", orders.size());

        List<OrderDTO> ordersWithPrices = orders.stream()
                .peek(order -> {
                    BigDecimal totalPrice = BigDecimal.ZERO;
                    if (order.getBookItems() != null && !order.getBookItems().isEmpty()) {
                        totalPrice = order.getBookItems().stream()
                                .map(item -> {
                                    try {
                                        BigDecimal bookPrice = bookPriceService.getBookPrice(item.getBookName().trim());
                                        BigDecimal itemTotal = bookPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                                        logger.debug("Book: {}, Price: {}, Quantity: {}, Item Total: {}",
                                                item.getBookName(), bookPrice, item.getQuantity(), itemTotal);
                                        return itemTotal;
                                    } catch (Exception e) {
                                        logger.warn("Failed to get price for book: {} - {}", item.getBookName(), e.getMessage());
                                        return BigDecimal.ZERO;
                                    }
                                })
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        order.setPrice(totalPrice);
                        logger.debug("Calculated total price for order (Client: {}, Date: {}): {}",
                                order.getClientEmail(), order.getOrderDate(), totalPrice);
                    } else {
                        logger.debug("No book items in order (Client: {}, Date: {})",
                                order.getClientEmail(), order.getOrderDate());
                    }
                })
                .collect(Collectors.toList());

        List<Employee> employees = orderService.getAllEmployees();
        logger.debug("Retrieved {} employees", employees.size());

        model.addAttribute("orders", ordersWithPrices);
        model.addAttribute("employees", employees);
        model.addAttribute("bookPriceService", bookPriceService);
        return "orders";
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String confirmOrder(@RequestParam("clientEmail") String clientEmail,
                               @RequestParam("orderDate") LocalDateTime orderDate,
                               @RequestParam("employeeEmail") String employeeEmail,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               @RequestParam(name = "lang", required = false) String lang) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Confirming order - Employee: {}, Roles: {}, Client: {}, Order Date: {}, Assigned Employee: {}",
                username, roles, clientEmail, orderDate, employeeEmail);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {}", lang);
        } else {
            logger.debug("Using default locale: {}", Locale.getDefault());
        }

        try {
            orderService.confirmOrder(clientEmail, orderDate, employeeEmail);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("order.confirmed", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", message);
            logger.info("Order confirmed successfully for client: {} at {}", clientEmail, orderDate);
        } catch (NotFoundException e) {
            logger.warn("Failed to confirm order - not found for client: {} at {}", clientEmail, orderDate);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("order.not.found", new Object[]{clientEmail, orderDate}, locale);
            redirectAttributes.addFlashAttribute("errorMessage", message);
        }
        return "redirect:/orders";
    }
}