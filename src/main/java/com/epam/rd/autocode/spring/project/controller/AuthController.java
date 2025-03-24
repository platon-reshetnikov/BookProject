package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.DuplicateResourceException;
import com.epam.rd.autocode.spring.project.mapper.UserWrapper;
import com.epam.rd.autocode.spring.project.service.impl.UserServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import validation.ClientValidationGroup;
import validation.EmployeeValidationGroup;

import java.util.Locale;
import java.util.Set;


@Controller
@RequestMapping("/register")
public class AuthController {

    private final UserServiceImpl userService;
    @Autowired
    private MessageSource messageSource;

    private final Validator validator;

    @Autowired
    public AuthController(UserServiceImpl userService,Validator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @GetMapping
    public String showRegistrationForm(
            @RequestParam(name = "lang", required = false) String lang,
            Model model) {
        if (!model.containsAttribute("userWrapper")) {
            model.addAttribute("userWrapper", new UserWrapper());
        }
        model.addAttribute("submitted", false);
        return "register";
    }

    @PostMapping
    public String registerUser(
            @RequestParam("userType") String userType,
            @ModelAttribute("userWrapper") UserWrapper userWrapper,
            BindingResult bindingResult,
            @RequestParam(name = "lang", required = false) String lang,
            Model model) {
        System.out.println("Обрабатываем регистрацию для типа пользователя: " + userType);
        if (lang != null && !lang.isBlank()) {
            System.out.println("Переключение локали на: " + lang + " (из POST /register)");
        } else {
            System.out.println("Использована локаль по умолчанию на POST /register: " + Locale.getDefault());
        }

        // Устанавливаем флаг submitted
        model.addAttribute("submitted", true);

        // Валидация в зависимости от userType
        if ("client".equals(userType)) {
            // Валидируем только clientDTO
            Set<ConstraintViolation<ClientDTO>> violations = validator.validate(userWrapper.getClientDTO(), ClientValidationGroup.class);
            for (ConstraintViolation<ClientDTO> violation : violations) {
                bindingResult.rejectValue("clientDTO." + violation.getPropertyPath(), violation.getMessageTemplate(), violation.getMessage());
            }
        } else if ("employee".equals(userType)) {
            // Валидируем только employeeDTO
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(userWrapper.getEmployeeDTO(), EmployeeValidationGroup.class);
            for (ConstraintViolation<EmployeeDTO> violation : violations) {
                bindingResult.rejectValue("employeeDTO." + violation.getPropertyPath(), violation.getMessageTemplate(), violation.getMessage());
            }
        } else {
            bindingResult.rejectValue("userType", "validation.user.type", "Please select a user type");
        }

        if (bindingResult.hasErrors()) {
            System.out.println("Ошибки валидации: " + bindingResult.getAllErrors());
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

    @GetMapping("/oauth2/success")
    public String oauth2Success(Model model) {
        // Handle successful OAuth2 login
        model.addAttribute("successMessage", "You have successfully logged in with Google!");
        return "books";
    }
}
