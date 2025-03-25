package com.epam.rd.autocode.spring.project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(HomeController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class HomeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageSource messageSource;


    @BeforeEach
    void setUp(){
        when(messageSource.getMessage(eq("welcome.message"), any(), eq("Welcome!"), eq(Locale.ENGLISH)))
                .thenReturn("Welcome!");
        when(messageSource.getMessage(eq("welcome.message"), any(), eq("Welcome!"), eq(new Locale("ru"))))
                .thenReturn("Добро пожаловать!");
        when(messageSource.getMessage(eq("welcome.message"), any(), eq("Welcome!"), eq(Locale.getDefault())))
                .thenReturn("Welcome by default!");
    }

    @Test
    void loginPage_WithLangParam_ReturnsLoginViewWithLocalizedMessage() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/login")
                        .param("lang", "ru"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("login"))
                .andExpect(MockMvcResultMatchers.model().attribute("welcomeMessage", "Добро пожаловать!"));
    }

    @Test
    void loginPage_WithAcceptLanguageHeader_ReturnsLoginViewWithLocalizedMessage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login")
                        .header("Accept-Language", "en-US"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("login"))
                .andExpect(MockMvcResultMatchers.model().attribute("welcomeMessage", "Welcome!"));
    }

    @Test
    void loginPage_WithMultipleAcceptLanguage_ReturnsLoginViewWithPrimaryLanguage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login")
                        .header("Accept-Language", "ru-RU, en-US;q=0.9"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("login"))
                .andExpect(MockMvcResultMatchers.model().attribute("welcomeMessage", "Добро пожаловать!"));
    }

    @Test
    void loginPage_WithoutLangOrHeader_ReturnsLoginViewWithDefaultLocale() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("login"))
                .andExpect(MockMvcResultMatchers.model().attribute("welcomeMessage", "Welcome by default!"));
    }

    @Test
    void loginPage_WithEmptyLangParam_ReturnsLoginViewWithDefaultLocale() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login")
                        .param("lang", ""))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("login"))
                .andExpect(MockMvcResultMatchers.model().attribute("welcomeMessage", "Welcome by default!"));
    }

    @Test
    void loginPage_WithInvalidAcceptLanguage_ReturnsLoginViewWithDefaultLocale() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login")
                        .header("Accept-Language", "invalid"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("login"))
                .andExpect(MockMvcResultMatchers.model().attribute("welcomeMessage", "Welcome by default!"));
    }


}
