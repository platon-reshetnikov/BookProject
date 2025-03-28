package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.auth.CustomUserDetailsService;
import com.epam.rd.autocode.spring.project.jwt.JwtAuthFilter;
import com.epam.rd.autocode.spring.project.jwt.JwtUtils;
import com.epam.rd.autocode.spring.project.provider.CustomAuthenticationProvider;
import com.epam.rd.autocode.spring.project.service.impl.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(JwtUtils jwtUtils,
                          CustomOAuth2UserService customOAuth2UserService) {
        this.jwtUtils = jwtUtils;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Qualifier("userServiceImpl") UserDetailsService userDetailsService) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/api/auth/**", "/login","/oauth2/**"
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/login", "/oauth2/**","/api/auth/**").permitAll()
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
                        .permitAll()
                        .successHandler((request, response, authentication) -> {
                            // For API requests, we return JSON with a token
                            if (isApiRequest(request)) {
                                String jwt = jwtUtils. generateToken(authentication);
                                response.setContentType("application/json");
                                response.getWriter().write(
                                        String.format("{\"token\":\"%s\"}", jwt)
                                );
                            } else {
                                // For regular requests - redirect
                                response.sendRedirect("/books");
                            }
                        })
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
                .addFilterBefore(jwtAuthFilter(userDetailsService), UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/logout")  // Must match your logout request URL
                        .logoutSuccessUrl("/login?logout=true")  // Must point to a valid endpoint
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET")) // Allow GET for logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // For API
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // For UI
                        .sessionFixation().migrateSession()  // Prevents session fixation attacks
                        .maximumSessions(1)                  // Allows only 1 session per user
                        .maxSessionsPreventsLogin(false)     // Terminates oldest session when new one starts
                        .expiredUrl("/login?expired")        // Redirect when session is invalidated
                )
                // Handling authentication errors
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (isApiRequest(request)) {
                                response.setContentType("application/json");
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.getWriter().write("{\"error\":\"Unauthorized\"}");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (isApiRequest(request)) {
                                response.setContentType("application/json");
                                response.setStatus(HttpStatus.FORBIDDEN.value());
                                response.getWriter().write("{\"error\":\"Forbidden\"}");
                            } else {
                                response.sendRedirect("/access-denied");
                            }
                        })
                )
                .requiresChannel(channel -> channel
                        .anyRequest().requiresSecure() // Force HTTPS for all requests
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(UserDetailsService userDetailsService) {
        return new JwtAuthFilter(jwtUtils, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        String contentType = request.getContentType();
        return request.getRequestURI().startsWith("/api/")
                && (acceptHeader != null && acceptHeader.contains("application/json"))
                || (contentType != null && contentType.contains("application/json"));
    }
}
