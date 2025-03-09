package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientRegistrationDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeRegistrationDTO;
import com.epam.rd.autocode.spring.project.exception.DuplicateResourceException;
import com.epam.rd.autocode.spring.project.mapper.UserWrapper;
import com.epam.rd.autocode.spring.project.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
    private MessageSource messageSource;

    @Autowired
    public AuthController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showRegistrationForm(
            @RequestParam(name = "lang", required = false) String lang,
            Model model) {
        System.out.println("Показываем форму регистрации");
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
        System.out.println("UserWrapper в модели: " + model.getAttribute("userWrapper"));
        return "register";
    }

    @PostMapping
    public String registerUser(
            @RequestParam("userType") String userType,
            @Valid @ModelAttribute("userWrapper") UserWrapper userWrapper,
            BindingResult bindingResult,
            @RequestParam(name = "lang", required = false) String lang,
            Model model) {
        System.out.println("Обрабатываем регистрацию для типа пользователя: " + userType);
        if (lang != null && !lang.isBlank()) {
            System.out.println("Переключение локали на: " + lang + " (из POST /register)");
        } else {
            System.out.println("Использована локаль по умолчанию на POST /register: " + Locale.getDefault());
        }

        if (bindingResult.hasErrors()) {
            System.out.println("Ошибки валидации: " + bindingResult.getAllErrors());
            model.addAttribute("error", messageSource.getMessage("validation.required", null, Locale.getDefault()));
            return "register";
        }

        Locale locale = Locale.getDefault();
        if (lang != null && !lang.isBlank()) {
            locale = new Locale(lang);
        }

        if ("client".equals(userType)) {
            ClientDTO clientDTO = userWrapper.getClientDTO();
            System.out.println("Получен ClientDTO: " + clientDTO);
            try {
                userService.addClient(clientDTO);
                System.out.println("Регистрация клиента прошла успешно");
                model.addAttribute("successMessage", messageSource.getMessage("register.client.success", null, locale));
                return "register";
            } catch (DuplicateResourceException e) {
                System.out.println("Регистрация клиента провалилась из-за дубликата: " + e.getMessage());
                model.addAttribute("error", messageSource.getMessage("register.duplicate.error", new Object[]{clientDTO.getEmail()}, locale));
                return "register";
            } catch (RuntimeException e) {
                System.out.println("Регистрация клиента провалилась: " + e.getMessage());
                model.addAttribute("error", messageSource.getMessage("register.error", new Object[]{e.getMessage()}, locale));
                return "register";
            }
        } else if ("employee".equals(userType)) {
            EmployeeDTO employeeDTO = userWrapper.getEmployeeDTO();
            System.out.println("Получен EmployeeDTO: " + employeeDTO);
            try {
                userService.addEmployee(employeeDTO);
                System.out.println("Регистрация сотрудника прошла успешно");
                model.addAttribute("successMessage", messageSource.getMessage("register.employee.success", null, locale));
                return "register";
            } catch (DuplicateResourceException e) {
                System.out.println("Регистрация сотрудника провалилась из-за дубликата: " + e.getMessage());
                model.addAttribute("error", messageSource.getMessage("register.duplicate.error", new Object[]{employeeDTO.getEmail()}, locale));
                return "register";
            } catch (RuntimeException e) {
                System.out.println("Регистрация сотрудника провалилась: " + e.getMessage());
                model.addAttribute("error", messageSource.getMessage("register.error", new Object[]{e.getMessage()}, locale));
                return "register";
            }
        } else {
            model.addAttribute("error", messageSource.getMessage("register.invalid.type", null, locale));
            return "register";
        }
    }
}
