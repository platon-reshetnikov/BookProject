package com.epam.rd.autocode.spring.project.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        if (locale == null) locale = Locale.getDefault();
        String entityType = determineEntityType(ex.getMessage());
        String key = entityType.toLowerCase() + ".not.found";
        String identifier = extractIdentifier(ex.getMessage());
        String errorMessage = messageSource.getMessage(key, new Object[]{identifier}, ex.getMessage(), locale);

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExistsException(AlreadyExistException ex, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        if (locale == null) locale = Locale.getDefault();
        String entityType = determineEntityType(ex.getMessage());
        String key = entityType.toLowerCase() + ".already.exists";
        String identifier = extractIdentifier(ex.getMessage());
        String errorMessage = messageSource.getMessage(key, new Object[]{identifier}, ex.getMessage(), locale);

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(), errorMessage, System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        if (locale == null) locale = Locale.getDefault();
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", messageSource.getMessage("validation.failed", null, "Validation failed", locale));
        response.put("errors", errors);
        response.put("timestamp", System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        if (locale == null) locale = Locale.getDefault();
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", messageSource.getMessage("validation.failed", null, "Validation failed", locale));
        response.put("errors", errors);
        response.put("timestamp", System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private String determineEntityType(String message) {
        if (message.contains("Employee")) return "Employee";
        if (message.contains("Client")) return "Client";
        if (message.contains("Book")) return "Book";
        if (message.contains("Order")) return "Order";
        return "Resource";
    }

    private String extractIdentifier(String message) {
        int start = message.indexOf(": ") + 2;
        if (start > 1 && start < message.length()) {
            return message.substring(start).trim();
        }
        return message;
    }
}
