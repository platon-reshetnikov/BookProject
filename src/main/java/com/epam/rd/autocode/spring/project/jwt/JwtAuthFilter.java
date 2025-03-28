package com.epam.rd.autocode.spring.project.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Get the token from the header
            final String authHeader = request.getHeader(AUTH_HEADER);
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Extracting JWT
            final String jwt = authHeader.substring(BEARER_PREFIX.length());
            logger.debug("Processing JWT token for request to: " + request.getRequestURI());

            // 3. Extracting email from token
            final String userEmail = jwtUtils.getUsernameFromToken(jwt);

            // 4. If there is an email and the user is not yet authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Загружаем данные пользователя
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Loading user data
                if (!userDetails.isEnabled()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account is disabled");
                    return;
                }

                // 6. Checking the validity of the token
                if (jwtUtils.validateToken(jwt, userDetails)) {
                    logger.debug("Valid JWT token for user: " + userEmail);

                    // 7. Create an authentication object
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // 8.Adding request details
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 9. Adding request details
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException e) {
            logger.error("Invalid JWT token: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        } catch (UsernameNotFoundException e) {
            logger.error("User not found: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        // 10. Let's continue the filter chain
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Пропускаем фильтр для публичных эндпоинтов
        return request.getServletPath().startsWith("/api/auth") ||
                request.getServletPath().startsWith("/login") ||
                request.getServletPath().startsWith("/oauth2") ||
                request.getServletPath().equals("/register");
    }
}