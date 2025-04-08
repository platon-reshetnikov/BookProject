package com.epam.rd.autocode.spring.project.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("(execution(* com.epam.rd.autocode.spring.project.service.impl.*.*(..)) || " +
            "execution(* com.epam.rd.autocode.spring.project.controller.*.*(..))) && " +
            "!execution(* com.epam.rd.autocode.spring.project.exception.GlobalExceptionHandler.*(..))")
    public void applicationMethods() {}

    @Before("applicationMethods()")
    public void logMethodStart(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        logger.info("Starting method: {} with arguments: {}", methodName, Arrays.toString(args));
    }

    @AfterReturning(pointcut = "applicationMethods()", returning = "result")
    public void logMethodSuccess(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        logger.info("Method {} completed successfully with result: {}", methodName, result);
    }

    @AfterThrowing(pointcut = "applicationMethods()", throwing = "ex")
    public void logMethodException(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        logger.warn("Method {} threw exception: {}", methodName, ex.getMessage());
    }

    @Around("applicationMethods()")
    public Object logMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        logger.debug("Method {} executed in {} ms", methodName, executionTime);

        return result;
    }
}