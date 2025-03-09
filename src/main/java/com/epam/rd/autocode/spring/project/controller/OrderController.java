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
        logger.info("Fetching orders for client: {}", clientEmail);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из /orders/client/{})", lang, clientEmail);
        } else {
            logger.info("Использована локаль по умолчанию на /orders/client/{}: {}", clientEmail, Locale.getDefault());
        }
        try {
            List<OrderDTO> orders = orderService.getOrdersByClient(clientEmail);
            model.addAttribute("orders", orders);
            model.addAttribute("clientEmail", clientEmail);
        } catch (NotFoundException e) {
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
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из /orders)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на /orders: {}", Locale.getDefault());
        }
        List<OrderDTO> orders = orderService.getAllOrders();

        List<OrderDTO> ordersWithPrices = orders.stream()
                .peek(order -> {
                    BigDecimal totalPrice = BigDecimal.ZERO;
                    if (order.getBookItems() != null && !order.getBookItems().isEmpty()) {
                        totalPrice = order.getBookItems().stream()
                                .map(item -> {
                                    try {
                                        BigDecimal bookPrice = bookPriceService.getBookPrice(item.getBookName().trim());
                                        return bookPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                                    } catch (Exception e) {
                                        return BigDecimal.ZERO;
                                    }
                                })
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        order.setPrice(totalPrice);
                    }
                })
                .collect(Collectors.toList());

        List<Employee> employees = orderService.getAllEmployees();
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
        logger.info("Employee {} confirming order for client {} at {}", employeeEmail, clientEmail, orderDate);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из POST /orders/confirm)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на POST /orders/confirm: {}", Locale.getDefault());
        }
        try {
            orderService.confirmOrder(clientEmail, orderDate, employeeEmail);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("order.confirmed", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (NotFoundException e) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("order.not.found", new Object[]{clientEmail, orderDate}, locale);
            redirectAttributes.addFlashAttribute("errorMessage", message);
        }
        return "redirect:/orders";
    }
}