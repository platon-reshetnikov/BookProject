package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.service.impl.UserServiceImpl;
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
        String email = authentication.getName(); // Get the email of the logged-in user

        // Fetch user details based on role
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CLIENT"))) {
            Client client = userService.getClientByEmail(email);
            model.addAttribute("user", client);
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("EMPLOYEE"))) {
            Employee employee = userService.getEmployeeByEmail(email);
            model.addAttribute("user", employee);
        }

        return "profile"; // Thymeleaf template for viewing profile
    }

    // Edit profile form
    @GetMapping("/edit")
    public String editProfileForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Get the email of the logged-in user

        // Fetch user details based on role
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CLIENT"))) {
            Client client = userService.getClientByEmail(email);
            model.addAttribute("user", new ClientDTO(client.getEmail(), client.getPassword(), client.getName(), client.getBalance()));
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("EMPLOYEE"))) {
            Employee employee = userService.getEmployeeByEmail(email);
            EmployeeDTO employeeDTO = EmployeeDTO.builder()
                    .email(employee.getEmail())
                    .password(employee.getPassword())
                    .name(employee.getName())
                    .phone(employee.getPhone())
                    .birthDate(employee.getBirthDate())
                    .build();
            model.addAttribute("user", employeeDTO);
        }
        return "edit-profile"; // Thymeleaf template for editing profile
    }

    // Save edited profile
    @PostMapping("/edit")
    public String saveProfile(
            @RequestParam("type") String type,
            @ModelAttribute("user") Object userDTO,
            BindingResult bindingResult) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; // Redirect to login if not authenticated
        }

        String email = authentication.getName();
        System.out.println("Received DTO: " + userDTO);

        if (bindingResult.hasErrors()) {
            System.out.println("Validation errors: " + bindingResult.getAllErrors());
            return "edit-profile"; // Return to form if validation fails
        }

        if ("client".equals(type) && userDTO instanceof ClientDTO clientDTO) {
            userService.updateClient(email, clientDTO);
        } else if ("employee".equals(type) && userDTO instanceof EmployeeDTO employeeDTO) {
            userService.updateEmployee(email, employeeDTO);
        }

        return "redirect:/profile"; // Redirect after saving
    }
}
