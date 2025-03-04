package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Отключаем CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/books", "/books/{name}").authenticated()
                        .requestMatchers("/profile", "/profile/edit").authenticated()
                        .requestMatchers("/basket/**").hasRole("CLIENT")
                        .requestMatchers("/orders/submit").hasRole("CLIENT")
                        .requestMatchers("/orders/client/{clientEmail}").authenticated()
                        .requestMatchers("/orders/employee/{employeeEmail}").authenticated()
                        .requestMatchers("/orders/order-date/{orderDate}").authenticated()
                        .requestMatchers("/clients", "/clients/{email}", "/clients/basket", "/clients/basket/add/{bookName}", "/clients/basket/clear", "/clients/basket/submit", "/clients/delete").authenticated()
                        .requestMatchers("/profile/delete").hasRole("CLIENT")
                        .requestMatchers("/clients/delete").hasRole("CLIENT")
                        .requestMatchers("/books/add", "/books/edit/{name}", "/books/delete/{name}").hasRole("EMPLOYEE")
                        .requestMatchers("/orders", "/orders/confirm/{id}").hasRole("EMPLOYEE")
                        .requestMatchers("/clients/manage", "/clients/block/{email}", "/clients/unblock/{email}", "/clients/list").hasRole("EMPLOYEE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form // Включаем форму входа
                        .loginPage("/login") // Страница входа
                        .defaultSuccessUrl("/books", true)
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user = User.withUsername("user")
//                .password(passwordEncoder().encode("password")) // Пароль: password
//                .roles("USER") // Зарегистрированный пользователь
//                .build();
//
//        UserDetails client = User.withUsername("client")
//                .password(passwordEncoder().encode("password")) // Пароль: password
//                .roles("CLIENT") // Клиент
//                .build();
//
//        UserDetails employee = User.withUsername("employee")
//                .password(passwordEncoder().encode("password")) // Пароль: password
//                .roles("EMPLOYEE") // Сотрудник
//                .build();
//
//        return new InMemoryUserDetailsManager(user, client, employee);
//    }
}
