package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientRegistrationDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeRegistrationDTO;
import com.epam.rd.autocode.spring.project.mapper.UserWrapper;
import com.epam.rd.autocode.spring.project.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import validation.ClientValidationGroup;
import validation.EmployeeValidationGroup;

import java.util.Locale;


@Controller
@RequestMapping("/register")
public class AuthController {

    private final UserServiceImpl userService;

    @Autowired
    public AuthController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showRegistrationForm(
            @RequestParam(name = "lang", required = false) String lang,
            Model model) {
        System.out.println("Showing registration form");
        if (lang != null && !lang.isBlank()) {
            System.out.println("Переключение локали на: " + lang + " (из /register)");
        } else {
            System.out.println("Использована локаль по умолчанию на /register: " + Locale.getDefault());
        }
        if (!model.containsAttribute("userWrapper")) {
            UserWrapper userWrapper = new UserWrapper();
            model.addAttribute("userWrapper", userWrapper);
        }
        model.addAttribute("submitted", false);
        System.out.println("UserWrapper in model: " + model.getAttribute("userWrapper"));
        return "register";
    }

    @PostMapping
    public String registerUser(
            @RequestParam("userType") String userType,
            @Valid @ModelAttribute("userWrapper") UserWrapper userWrapper,
            BindingResult bindingResult,
            @RequestParam(name = "lang", required = false) String lang,
            Model model) {
        System.out.println("Processing registration for user type: " + userType);
        if (lang != null && !lang.isBlank()) {
            System.out.println("Переключение локали на: " + lang + " (из POST /register)");
        } else {
            System.out.println("Использована локаль по умолчанию на POST /register: " + Locale.getDefault());
        }

        if (bindingResult.hasErrors()) {
            System.out.println("Validation errors: " + bindingResult.getAllErrors());
            model.addAttribute("error", "Please fix the validation errors.");
            return "register";
        }

        if ("client".equals(userType)) {
            ClientDTO clientDTO = userWrapper.getClientDTO();
            System.out.println("ClientDTO received: " + clientDTO);
            try {
                userService.addClient(clientDTO);
                System.out.println("Client registration successful");
                model.addAttribute("successMessage", "Client registered successfully!");
            } catch (RuntimeException e) {
                System.out.println("Client registration failed: " + e.getMessage());
                model.addAttribute("error", e.getMessage());
                return "register";
            }
        } else if ("employee".equals(userType)) {
            EmployeeDTO employeeDTO = userWrapper.getEmployeeDTO();
            System.out.println("EmployeeDTO received: " + employeeDTO);
            try {
                userService.addEmployee(employeeDTO);
                System.out.println("Employee registration successful");
                model.addAttribute("successMessage", "Employee registered successfully!");
            } catch (RuntimeException e) {
                System.out.println("Employee registration failed: " + e.getMessage());
                model.addAttribute("error", e.getMessage());
                return "register";
            }
        } else {
            model.addAttribute("error", "Invalid user type selected.");
            return "register";
        }

        return "redirect:/login";
    }
}
