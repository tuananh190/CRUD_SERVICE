package com.mar.CRUD_SERVICE.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Log at INFO so it's visible by default when debugging requests
            if (authHeader == null) {
                log.info("No Authorization header present for request {} {}", request.getMethod(), request.getRequestURI());
            } else {
                log.info("Authorization header present but does not start with 'Bearer ': '{}' for request {} {}", authHeader, request.getMethod(), request.getRequestURI());
            }
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractUsername(jwt);
            log.debug("Extracted username '{}' from JWT for request {} {}", userEmail, request.getMethod(), request.getRequestURI());
        } catch (Exception ex) {
            // If token parsing fails (malformed/invalid), log and continue the filter chain without authentication.
            log.warn("Failed to extract username from JWT: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("JWT validated and authentication set for user '{}' on request {} {}", userEmail, request.getMethod(), request.getRequestURI());
                } else {
                    log.warn("JWT token is not valid for user '{}'", userEmail);
                }
            } catch (Exception ex) {
                // Log the failure and continue without setting authentication
                log.warn("Failed to load user or validate token for {}: {}", userEmail, ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}