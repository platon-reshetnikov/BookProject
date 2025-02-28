package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.MapStruct.EmployeeMapper;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final MessageSource messageSource;

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(employeeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee not found with email: " + email));
        return employeeMapper.toDTO(employee);
    }

    @Override
    public EmployeeDTO updateEmployeeByEmail(String email, @Valid EmployeeDTO employeeDTO) {
        Employee existingEmployee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee not found with email: " + email));
        employeeMapper.updateEntityFromDTO(employeeDTO, existingEmployee);
        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return employeeMapper.toDTO(updatedEmployee);
    }

    @Override
    public void deleteEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee not found with email: " + email));
        employeeRepository.delete(employee);
    }

    @Override
    public EmployeeDTO addEmployee(@Valid EmployeeDTO employeeDTO) {
        if (employeeRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
            throw new AlreadyExistException("Employee already exists with email: " + employeeDTO.getEmail());
        }
        Employee employee = employeeMapper.toEntity(employeeDTO);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDTO(savedEmployee);
    }

    @Override
    public List<EmployeeDTO> getEmployeesByBirthDate(LocalDate birthDate) {
        List<Employee> employees = employeeRepository.findByBirthDate(birthDate);
        return employees.stream()
                .map(employeeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public String getEmployeeNotFoundMessage(Locale locale) {
        return messageSource.getMessage("employee.not.found", null, locale);
    }
}
