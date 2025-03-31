package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.jwt.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
@EnableWebSecurity
public class SecurityConfigTestJWT {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    @Primary
    public JwtUtils jwtUtils() {
        return new JwtUtils() {
            @Override
            public String generateToken(Authentication authentication) {
                return "test-token";
            }

            public boolean validateToken(String token) {
                return true;
            }

            @Override
            public String getUsernameFromToken(String token) {
                return "test-user";
            }
        };
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("test-user")
                        .password("password")
                        .roles("USER")
                        .build()
        );
    }
}