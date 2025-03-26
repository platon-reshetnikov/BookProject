package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.auth.CustomUserDetailsService;
import com.epam.rd.autocode.spring.project.service.impl.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomUserDetailsService userDetailsService, CustomOAuth2UserService customOAuth2UserService) {
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/oauth2/**"  // OAuth2 endpoints typically don't need CSRF
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/login", "/oauth2/**").permitAll()
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
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // Redirect to the custom login page
                        .defaultSuccessUrl("/books", true) // Redirect after successful OAuth2 login
                        .failureUrl("/login?error=true") // Redirect on OAuth2 login failure
                        .authorizationEndpoint(auth -> auth // Authorization Endpoint Configuration
                                .baseUri("/oauth2/authorization")
                                .authorizationRequestRepository(authorizationRequestRepository())
                        )
                        .userInfoEndpoint(userInfo -> userInfo // User Info Endpoint Configuration
                                .userService(customOAuth2UserService)
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")  // Must match your logout request URL
                        .logoutSuccessUrl("/login?logout=true")  // Must point to a valid endpoint
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET")) // Allow GET for logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()  // Prevents session fixation attacks
                        .maximumSessions(1)                  // Allows only 1 session per user
                        .maxSessionsPreventsLogin(false)     // Terminates oldest session when new one starts
                        .expiredUrl("/login?expired")        // Redirect when session is invalidated
                )
                .requiresChannel(channel -> channel
                        .anyRequest().requiresSecure() // Force HTTPS for all requests
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }
}
