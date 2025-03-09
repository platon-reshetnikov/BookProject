package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.mapper.UserWrapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.service.impl.UserServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final UserServiceImpl userService;

    @Autowired
    private MessageSource messageSource;

    public ProfileController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping
    public String viewProfile(Model model, @RequestParam(name = "lang", required = false) String lang) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        logger.info("Viewing profile for user: {}, Roles: {}", email, authentication.getAuthorities());
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из /profile)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на /profile: {}", Locale.getDefault());
        }

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
            Client client = userService.getClientByEmail(email);
            model.addAttribute("user", client);
            model.addAttribute("userType", "client");
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"))) {
            Employee employee = userService.getEmployeeByEmail(email);
            model.addAttribute("user", employee);
            model.addAttribute("userType", "employee");
        }
        return "profile";
    }

    @GetMapping("/edit")
    public String editProfileForm(Model model, @RequestParam(name = "lang", required = false) String lang) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        logger.info("Editing profile form for user: {}, Roles: {}", email, authentication.getAuthorities());
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из /profile/edit)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на /profile/edit: {}", Locale.getDefault());
        }

        UserWrapper userWrapper = new UserWrapper();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
            Client client = userService.getClientByEmail(email);
            ClientDTO clientDTO = new ClientDTO(client.getEmail(), client.getPassword(), client.getName(), client.getBalance());
            logger.info("ClientDTO created: {}", clientDTO);
            userWrapper.setClientDTO(clientDTO);
            model.addAttribute("userType", "client");
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"))) {
            Employee employee = userService.getEmployeeByEmail(email);
            EmployeeDTO employeeDTO = new EmployeeDTO(employee.getEmail(), employee.getPassword(), employee.getName(), employee.getPhone(), employee.getBirthDate());
            logger.info("EmployeeDTO created: {}", employeeDTO);
            String formattedBirthDate = employee.getBirthDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            model.addAttribute("formattedBirthDate", formattedBirthDate);
            userWrapper.setEmployeeDTO(employeeDTO);
            model.addAttribute("userType", "employee");
        }

        model.addAttribute("userWrapper", userWrapper);
        return "edit-profile";
    }

    @PostMapping("/edit")
    public String saveProfile(
            @RequestParam("userType") String userType,
            @ModelAttribute("userWrapper") UserWrapper userWrapper,
            BindingResult bindingResult,
            Model model,
            @RequestParam(name = "lang", required = false) String lang) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        logger.info("Saving profile for user: {}, userType: {}", email, userType);
        if (lang != null && !lang.isBlank()) {
            logger.info("Переключение локали на: {} (из POST /profile/edit)", lang);
        } else {
            logger.info("Использована локаль по умолчанию на POST /profile/edit: {}", Locale.getDefault());
        }

        if ("client".equals(userType)) {
            ClientDTO clientDTO = userWrapper.getClientDTO();
            if (clientDTO == null) {
                logger.error("clientDTO is null for user: {}", email);
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("profile.client.missing", null, locale);
                model.addAttribute("error", message);
                return "edit-profile";
            }

            if (bindingResult.hasErrors()) {
                logger.error("Client validation errors: {}", bindingResult.getAllErrors());
                model.addAttribute("userWrapper", userWrapper);
                model.addAttribute("userType", userType);
                return "edit-profile";
            }

            try {
                userService.updateClient(email, clientDTO);
            } catch (Exception e) {
                logger.error("Error saving client profile: {}", e.getMessage());
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("profile.save.error", new Object[]{e.getMessage()}, locale);
                model.addAttribute("error", message);
                return "edit-profile";
            }
        } else if ("employee".equals(userType)) {
            EmployeeDTO employeeDTO = userWrapper.getEmployeeDTO();
            if (employeeDTO == null) {
                logger.error("employeeDTO is null for user: {}", email);
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("profile.employee.missing", null, locale);
                model.addAttribute("error", message);
                return "edit-profile";
            }

            if (bindingResult.hasErrors()) {
                logger.error("Employee validation errors: {}", bindingResult.getAllErrors());
                model.addAttribute("userWrapper", userWrapper);
                model.addAttribute("userType", userType);
                return "edit-profile";
            }

            try {
                userService.updateEmployee(email, employeeDTO);
            } catch (Exception e) {
                logger.error("Error saving employee profile: {}", e.getMessage());
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("profile.save.error", new Object[]{e.getMessage()}, locale);
                model.addAttribute("error", message);
                return "edit-profile";
            }
        } else {
            logger.error("Invalid userType: {}", userType);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("profile.invalid.type", new Object[]{userType}, locale);
            model.addAttribute("error", message);
            return "edit-profile";
        }

        return "redirect:/profile";
    }
}
