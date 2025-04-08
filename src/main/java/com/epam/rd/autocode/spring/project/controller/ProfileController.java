package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.mapper.UserWrapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.service.UserService;
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
    private final UserService userService;
    @Autowired
    private MessageSource messageSource;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String viewProfile(Model model, @RequestParam(name = "lang", required = false) String lang) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

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
        UserWrapper userWrapper = new UserWrapper();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
            Client client = userService.getClientByEmail(email);
            ClientDTO clientDTO = new ClientDTO(client.getEmail(), client.getPassword(), client.getName(), client.getBalance());
            userWrapper.setClientDTO(clientDTO);
            model.addAttribute("userType", "client");
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"))) {
            Employee employee = userService.getEmployeeByEmail(email);
            EmployeeDTO employeeDTO = new EmployeeDTO(employee.getEmail(), employee.getPassword(), employee.getName(), employee.getPhone(), employee.getBirthDate());
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

        if ("client".equals(userType)) {
            ClientDTO clientDTO = userWrapper.getClientDTO();
            if (clientDTO == null) {
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("profile.client.missing", null, locale);
                model.addAttribute("error", message);
                return "edit-profile";
            }
            if (bindingResult.hasErrors()) {
                model.addAttribute("userWrapper", userWrapper);
                model.addAttribute("userType", userType);
                return "edit-profile";
            }
            try {
                userService.updateClient(email, clientDTO);
            } catch (Exception e) {
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("profile.save.error", new Object[]{e.getMessage()}, locale);
                model.addAttribute("error", message);
                return "edit-profile";
            }
        } else if ("employee".equals(userType)) {
            EmployeeDTO employeeDTO = userWrapper.getEmployeeDTO();
            if (employeeDTO == null) {
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("profile.employee.missing", null, locale);
                model.addAttribute("error", message);
                return "edit-profile";
            }
            if (bindingResult.hasErrors()) {
                model.addAttribute("userWrapper", userWrapper);
                model.addAttribute("userType", userType);
                return "edit-profile";
            }
            try {
                userService.updateEmployee(email, employeeDTO);
            } catch (Exception e) {
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("profile.save.error", new Object[]{e.getMessage()}, locale);
                model.addAttribute("error", message);
                return "edit-profile";
            }
        } else {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("profile.invalid.type", new Object[]{userType}, locale);
            model.addAttribute("error", message);
            return "edit-profile";
        }
        return "redirect:/profile";
    }
}