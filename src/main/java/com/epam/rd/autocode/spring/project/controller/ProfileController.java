package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserServiceImpl userService;

    public ProfileController(UserServiceImpl userService) {
        this.userService = userService;
    }

    // View profile
    @GetMapping
    public String viewProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CLIENT"))) {
            Client client = userService.getClientByEmail(email);
            model.addAttribute("user", client);
            model.addAttribute("userType", "client");
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("EMPLOYEE"))) {
            Employee employee = userService.getEmployeeByEmail(email);
            model.addAttribute("user", employee);
            model.addAttribute("userType", "employee");
        }

        return "profile";
    }

    // Edit profile form
    @GetMapping("/edit")
    public String editProfileForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CLIENT"))) {
            Client client = userService.getClientByEmail(email);
            ClientDTO clientDTO = new ClientDTO(client.getEmail(), client.getPassword(), client.getName(), client.getBalance());
            model.addAttribute("client", clientDTO);
            model.addAttribute("userType", "client");
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("EMPLOYEE"))) {
            Employee employee = userService.getEmployeeByEmail(email);
            EmployeeDTO employeeDTO = new EmployeeDTO(employee.getEmail(), employee.getPassword(), employee.getName(), employee.getPhone(), employee.getBirthDate());
            model.addAttribute("employee", employeeDTO);
            model.addAttribute("userType", "employee");
        }
        return "edit-profile";
    }

    // Save edited profile for Client
    @PostMapping("/edit")
    public String saveClientProfile(
            @RequestParam("userType") String userType,
            @Valid @ModelAttribute("client") ClientDTO clientDTO,
            BindingResult bindingResultClient,
            @Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
            BindingResult bindingResultEmployee,
            Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();

        if ("client".equals(userType)) {
            if (bindingResultClient.hasErrors()) {
                model.addAttribute("userType", "client");
                return "edit-profile";
            }
            try {
                userService.updateClient(email, clientDTO);
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("userType", "client");
                return "edit-profile";
            }
        } else if ("employee".equals(userType)) {
            if (bindingResultEmployee.hasErrors()) {
                model.addAttribute("userType", "employee");
                return "edit-profile";
            }
            try {
                userService.updateEmployee(email, employeeDTO);
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("userType", "employee");
                return "edit-profile";
            }
        }

        return "redirect:/profile";
    }
}
