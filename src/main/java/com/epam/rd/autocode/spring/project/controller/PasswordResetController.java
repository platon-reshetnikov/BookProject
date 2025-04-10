package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Controller
public class PasswordResetController {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);
    private final UserService userService;
    private final MessageSource messageSource;

    public PasswordResetController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(@RequestParam(value = "lang", required = false) String lang, Model model) {
        logger.info("Showing forgot password form");
        model.addAttribute("email", "");
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam("email") String email, Model model) {
        logger.info("Processing forgot password request for email: {}", email);
        try {
            userService.generatePasswordResetToken(email);
            model.addAttribute("message", messageSource.getMessage(
                    "password.reset.request.sent", null, Locale.getDefault()));
        } catch (NotFoundException e) {
            logger.warn("Failed to generate reset token: {}", e.getMessage());
            model.addAttribute("error", messageSource.getMessage(
                    "client.not.found", new Object[]{email}, Locale.getDefault()));
        } catch (Exception e) {
            logger.error("Unexpected error during forgot password request: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to send reset email: " + e.getMessage());
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token,
                                        @RequestParam(value = "lang", required = false) String lang,
                                        Model model) {
        logger.info("Showing reset password form for token: {}", token);
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                @RequestParam("password") String newPassword,
                                Model model) {
        logger.info("Processing password reset for token: {}", token);
        try {
            userService.resetPassword(token, newPassword);
            logger.info("Password reset successful for token: {}", token);
            model.addAttribute("successMessage", messageSource.getMessage(
                    "password.reset.success", null, Locale.getDefault()));
            return "login";
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        } catch (Exception e) {
            logger.error("Unexpected error during password reset: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "An unexpected error occurred");
            model.addAttribute("token", token);
            return "reset-password";
        }
    }
}