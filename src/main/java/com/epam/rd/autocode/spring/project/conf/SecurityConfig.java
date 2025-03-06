package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.auth.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                .csrf(AbstractHttpConfigurer::disable)
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
                .formLogin(form -> form
                        .loginPage("/login")
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
}
