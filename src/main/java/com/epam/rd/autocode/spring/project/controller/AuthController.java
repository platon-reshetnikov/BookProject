package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.DuplicateResourceException;
import com.epam.rd.autocode.spring.project.mapper.UserWrapper;
import com.epam.rd.autocode.spring.project.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    @Autowired
    private MessageSource messageSource;
    private final Validator validator;

    @Autowired
    public AuthController(UserService userService,Validator validator) {
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
        logger.info("Processing registration for user type: {}", userType);

        if (lang != null && !lang.isBlank()) {
            logger.debug("Switching locale to: {} (from POST /register)", lang);
        } else {
            logger.debug("Using default locale for POST /register: {}", Locale.getDefault());
        }

        model.addAttribute("submitted", true);

        if ("client".equals(userType)) {
            Set<ConstraintViolation<ClientDTO>> violations = validator.validate(userWrapper.getClientDTO(), ClientValidationGroup.class);
            for (ConstraintViolation<ClientDTO> violation : violations) {
                bindingResult.rejectValue("clientDTO." + violation.getPropertyPath(), violation.getMessageTemplate(), violation.getMessage());
            }
        } else if ("employee".equals(userType)) {
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(userWrapper.getEmployeeDTO(), EmployeeValidationGroup.class);
            for (ConstraintViolation<EmployeeDTO> violation : violations) {
                bindingResult.rejectValue("employeeDTO." + violation.getPropertyPath(), violation.getMessageTemplate(), violation.getMessage());
            }
        } else {
            bindingResult.rejectValue("userType", "validation.user.type", "Please select a user type");
        }

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred: {}", bindingResult.getAllErrors());
            return "register";
        }

        Locale locale = Locale.getDefault();
        if (lang != null && !lang.isBlank()) {
            locale = new Locale(lang);
        }

        if ("client".equals(userType)) {
            ClientDTO clientDTO = userWrapper.getClientDTO();
            logger.debug("Received ClientDTO: {}", clientDTO);
            try {
                userService.addClient(clientDTO);
                logger.info("Client registration successful");
                model.addAttribute("successMessage", messageSource.getMessage("register.client.success", null, locale));
                return "register";
            } catch (DuplicateResourceException e) {
                logger.warn("Client registration failed due to duplicate: {}", e.getMessage());
                model.addAttribute("error", messageSource.getMessage("register.duplicate.error", new Object[]{clientDTO.getEmail()}, locale));
                return "register";
            } catch (RuntimeException e) {
                logger.error("Client registration failed: {}", e.getMessage());
                model.addAttribute("error", messageSource.getMessage("register.error", new Object[]{e.getMessage()}, locale));
                return "register";
            }
        } else if ("employee".equals(userType)) {
            EmployeeDTO employeeDTO = userWrapper.getEmployeeDTO();
            logger.debug("Received EmployeeDTO: {}", employeeDTO);
            try {
                userService.addEmployee(employeeDTO);
                logger.info("Employee registration successful");
                model.addAttribute("successMessage", messageSource.getMessage("register.employee.success", null, locale));
                return "register";
            } catch (DuplicateResourceException e) {
                logger.warn("Employee registration failed due to duplicate: {}", e.getMessage());
                model.addAttribute("error", messageSource.getMessage("register.duplicate.error", new Object[]{employeeDTO.getEmail()}, locale));
                return "register";
            } catch (RuntimeException e) {
                logger.error("Employee registration failed: {}", e.getMessage());
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
        model.addAttribute("successMessage", "You have successfully logged in with Google!");
        return "books";
    }
}



