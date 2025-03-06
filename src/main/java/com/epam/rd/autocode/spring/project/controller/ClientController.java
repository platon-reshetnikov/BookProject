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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    @GetMapping("/manage")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getAllClients(Model model) {
        logger.info("Employee accessing all clients");
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
    public String blockClient(@PathVariable String email, Model model) {
        logger.info("Employee blocking client: {}", email);
        try {
            clientService.blockClient(email);
            model.addAttribute("successMessage", "Client " + email + " has been blocked");
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", "Client not found with email: " + email);
        }
        return "redirect:/clients/manage";
    }

    @PostMapping("/unblock/{email}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String unblockClient(@PathVariable String email, Model model) {
        logger.info("Employee unblocking client: {}", email);
        try {
            clientService.unblockClient(email);
            model.addAttribute("successMessage", "Client " + email + " has been unblocked");
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", "Client not found with email: " + email);
        }
        return "redirect:/clients/manage";
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getClientList(Model model) {
        logger.info("Employee accessing client list");
        List<ClientDTO> clients = clientService.getAllClients();
        model.addAttribute("clients", clients);
        return "client-list";
    }

    @PostMapping("/basket/add/{bookName}")
    @PreAuthorize("hasRole('CLIENT')")
    public String addBookToBasket(@PathVariable String bookName, @RequestParam(defaultValue = "1") int quantity, Model model) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Client {} adding book {} to basket", clientEmail, bookName);
        try {
            clientService.addBookToBasket(clientEmail, bookName, quantity);
            model.addAttribute("successMessage", "Book " + bookName + " added to basket");
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", "Client not found: " + clientEmail);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/books";
    }

    @GetMapping("/basket")
    @PreAuthorize("hasRole('CLIENT')")
    public String viewBasket(Model model) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Client {} viewing basket", clientEmail);
        List<BookItemDTO> basket = clientService.getBasket(clientEmail);
        model.addAttribute("basket", basket);
        return "basket";
    }

    @PostMapping("/basket/clear")
    @PreAuthorize("hasRole('CLIENT')")
    public String clearBasket(Model model) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Client {} clearing their basket", clientEmail);
        try {
            clientService.clearBasket(clientEmail);
            model.addAttribute("successMessage", "Your basket has been cleared");
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", "Client not found: " + clientEmail);
        }
        return "redirect:/clients/basket";
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('CLIENT')")
    public String deleteAccount(HttpServletRequest request, HttpServletResponse response, Model model) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Client {} deleting their account", clientEmail);
        try {
            clientService.deleteClientByEmail(clientEmail);
            new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
            return "redirect:/login?deleted";
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", "Client not found: " + clientEmail);
            return "profile";
        }
    }

    @PostMapping("/basket/submit")
    @PreAuthorize("hasRole('CLIENT')")
    public String submitOrder(Model model) {
        String clientEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            clientService.getClientByEmail(clientEmail);
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", "Your account no longer exists. Please log in again.");
            return "redirect:/login?error";
        }

        List<BookItemDTO> basket = clientService.getBasket(clientEmail);
        if (basket == null || basket.isEmpty()) {
            model.addAttribute("errorMessage", "Your basket is empty");
            return "basket";
        }

        try {
            List<Employee> employees = employeeRepository.findAll();
            if (employees.isEmpty()) {
                model.addAttribute("errorMessage", "No employees available to process your order");
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
            model.addAttribute("successMessage", "Your order has been submitted and assigned to " + employeeEmail);
            return "redirect:/clients/basket";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error submitting order: " + e.getMessage());
            return "basket";
        }
    }
}
