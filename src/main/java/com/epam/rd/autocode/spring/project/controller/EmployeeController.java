package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public List<EmployeeDTO> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getEmployeeByEmail(@PathVariable String email, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        EmployeeDTO employee = employeeService.getEmployeeByEmail(email);
        if (employee == null) {
            String message = messageSource.getMessage("employee.not.found", new Object[]{email}, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        return ResponseEntity.ok(employee);
    }

    @PostMapping
    public ResponseEntity<?> addEmployee(@Valid @RequestBody EmployeeDTO employeeDTO, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        EmployeeDTO savedEmployee = employeeService.addEmployee(employeeDTO);
        String message = messageSource.getMessage("employee.added", new Object[]{savedEmployee.getEmail()}, locale);
        return ResponseEntity.status(HttpStatus.CREATED).header("X-Message", message).body(savedEmployee);
    }

    @PutMapping("/{email}")
    public ResponseEntity<?> updateEmployee(@PathVariable String email, @Valid @RequestBody EmployeeDTO employeeDTO, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        EmployeeDTO updatedEmployee = employeeService.updateEmployeeByEmail(email, employeeDTO);
        if (updatedEmployee == null) {
            String message = messageSource.getMessage("employee.not.found", new Object[]{email}, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        String message = messageSource.getMessage("employee.updated", new Object[]{updatedEmployee.getEmail()}, locale);
        return ResponseEntity.ok().header("X-Message", message).body(updatedEmployee);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteEmployee(@PathVariable String email, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        try {
            employeeService.deleteEmployeeByEmail(email);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            String message = messageSource.getMessage("employee.not.found", new Object[]{email}, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @GetMapping("/birth-date/{birthDate}")
    public List<EmployeeDTO> getEmployeesByBirthDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate) {
        return employeeService.getEmployeesByBirthDate(birthDate);
    }
}
