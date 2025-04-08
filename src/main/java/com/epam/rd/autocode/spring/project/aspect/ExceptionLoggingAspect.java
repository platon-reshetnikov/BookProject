package com.epam.rd.autocode.spring.project.aspect;

import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.DuplicateResourceException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.stream.Collectors;

@Aspect
@Component
public class ExceptionLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionLoggingAspect.class);

    @AfterThrowing(pointcut = "execution(* com.epam.rd.autocode.spring.project.exception.GlobalExceptionHandler.*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        if (ex instanceof NotFoundException) {
            logger.warn("NotFoundException in {}: {}", methodName, ex.getMessage());
        } else if (ex instanceof AlreadyExistException) {
            logger.warn("AlreadyExistException in {}: {}", methodName, ex.getMessage());
        } else if (ex instanceof DuplicateResourceException) {
            logger.warn("DuplicateResourceException in {}: {}", methodName, ex.getMessage());
        } else if (ex instanceof MethodArgumentNotValidException validationEx) {
            logger.error("Validation failed in {}: MethodArgumentNotValidException - {}", methodName, ex.getMessage());
            String errors = validationEx.getBindingResult().getFieldErrors().stream()
                    .map(error -> "Field: " + error.getField() + ", Message: " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            logger.debug("Validation errors: {}", errors);
        } else if (ex instanceof ConstraintViolationException constraintEx) {
            logger.error("Validation failed in {}: ConstraintViolationException - {}", methodName, ex.getMessage());
            String errors = constraintEx.getConstraintViolations().stream()
                    .map(violation -> "Path: " + violation.getPropertyPath() + ", Message: " + violation.getMessage())
                    .collect(Collectors.joining("; "));
            logger.debug("Constraint violations: {}", errors);
        } else {
            logger.error("Unexpected exception in {}: {}", methodName, ex.getMessage(), ex);
        }
    }
}