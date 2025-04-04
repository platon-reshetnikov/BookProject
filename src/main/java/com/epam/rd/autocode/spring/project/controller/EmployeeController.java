package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private MessageSource messageSource;

    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteEmployee(@PathVariable String email,
                                            @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        try {
            employeeService.deleteEmployeeByEmail(email);
            String successMessage = messageSource.getMessage("employee.deleted", new Object[]{email},
                    "Employee with email " + email + " has been deleted successfully", locale);
            return ResponseEntity.ok().body(successMessage);
        } catch (NotFoundException e) {
            String errorMessage = messageSource.getMessage("employee.not.found", new Object[]{email},
                    "Employee not found with email: " + email, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
    }
}
