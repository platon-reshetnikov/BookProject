package com.epam.rd.autocode.spring.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Locale;

@Controller
public class HomeController {

    private final MessageSource messageSource;

    @Autowired
    public HomeController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping("/login")
    public String loginPage(@RequestHeader(name = "Accept-Language", required = false) String acceptLanguage,
                            Model model) {
        Locale locale = parseLocale(acceptLanguage);
        String welcomeMessage = messageSource.getMessage("welcome.message", null, "Welcome!", locale);
        model.addAttribute("welcomeMessage", welcomeMessage);
        return "login";
    }

    private Locale parseLocale(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return Locale.getDefault();
        }

        String primaryLanguage = acceptLanguage.split(",")[0].trim();
        String[] parts = primaryLanguage.split("-");

        return parts.length > 1 ? new Locale(parts[0], parts[1]) : new Locale(parts[0]);
    }
}
