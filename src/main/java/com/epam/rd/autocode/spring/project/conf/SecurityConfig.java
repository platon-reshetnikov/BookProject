package com.epam.rd.autocode.spring.project.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Отключаем CSRF
                .authorizeHttpRequests(auth -> auth
                        // Доступ для всех пользователей (включая незарегистрированных)
                        .requestMatchers("/books", "/books/{id}").permitAll() // Просмотр книг

                        // Доступ для зарегистрированных пользователей
                        .requestMatchers("/profile", "/profile/edit").authenticated() // Редактирование профиля

                        // Доступ для клиентов
                        .requestMatchers("/basket/**").hasRole("CLIENT") // Корзина
                        .requestMatchers("/orders/submit").hasRole("CLIENT") // Размещение заказов
                        .requestMatchers("/profile/delete").hasRole("CLIENT") // Удаление аккаунта

                        // Доступ для сотрудников
                        .requestMatchers("/books/add", "/books/edit/{id}", "/books/delete/{id}").hasRole("EMPLOYEE") // Управление книгами
                        .requestMatchers("/orders/confirm/{id}").hasRole("EMPLOYEE") // Подтверждение заказов
                        .requestMatchers("/customers", "/customers/block/{id}", "/customers/unblock/{id}").hasRole("EMPLOYEE") // Управление клиентами

                        // Все остальные запросы требуют авторизации
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form // Включаем форму входа
                        .loginPage("/login") // Страница входа
                        .permitAll()
                )
                .logout(logout -> logout // Включаем выход
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Используем BCrypt для шифрования паролей
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder().encode("password")) // Пароль: password
                .roles("USER") // Зарегистрированный пользователь
                .build();

        UserDetails client = User.withUsername("client")
                .password(passwordEncoder().encode("password")) // Пароль: password
                .roles("CLIENT") // Клиент
                .build();

        UserDetails employee = User.withUsername("employee")
                .password(passwordEncoder().encode("password")) // Пароль: password
                .roles("EMPLOYEE") // Сотрудник
                .build();

        return new InMemoryUserDetailsManager(user, client, employee);
    }
}
