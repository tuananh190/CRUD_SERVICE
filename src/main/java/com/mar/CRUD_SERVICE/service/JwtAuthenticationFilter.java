package com.mar.CRUD_SERVICE.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import com.mar.CRUD_SERVICE.repository.UserRepository;
import com.mar.CRUD_SERVICE.model.User;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtService jwtService, 
                                   UserDetailsService userDetailsService,
                                   TokenBlacklistService tokenBlacklistService,
                                   UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            if (authHeader == null) {
                log.info("No Authorization header present for request {} {}", request.getMethod(), request.getRequestURI());
            } else {
                log.info("Authorization header present but does not start with 'Bearer ': '{}' for request {} {}", authHeader, request.getMethod(), request.getRequestURI());
            }
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        // --- BƯỚC BẢO MẬT: KIỂM TRA BLACKLIST ---
        // Nếu token đã bị thu hồi (người dùng đã logout), chặn ngay lập tức
        if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
            log.warn("Attempt to use a blacklisted JWT token: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token đã hết hạn hoặc bị thu hồi (Logged out).");
            return;
        }

        try {
            username = jwtService.extractUsername(jwt);
            log.debug("Extracted username '{}' from JWT for request {} {}", username, request.getMethod(), request.getRequestURI());
        } catch (Exception ex) {

            log.warn("Failed to extract username from JWT: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // Lỗi 3: JWT Token Bypass - Kiểm tra TokenVersion
                Integer tokenVersionClaim = jwtService.extractClaim(jwt, claims -> claims.get("tokenVersion", Integer.class));
                Long tokenVersion = tokenVersionClaim != null ? tokenVersionClaim.longValue() : null;
                
                User user = userRepository.findByUsername(username).orElse(null);
                
                if (user != null && tokenVersion != null && !tokenVersion.equals(user.getTokenVersion())) {
                    log.warn("Attempt to use an outdated JWT token (Password was changed): {}", request.getRequestURI());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token đã bị vô hiệu hóa do mật khẩu thay đổi. Vui lòng đăng nhập lại.");
                    return;
                }

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("JWT validated and authentication set for user '{}' on request {} {}", username, request.getMethod(), request.getRequestURI());
                } else {
                    log.warn("JWT token is not valid for user '{}'", username);
                }
            } catch (Exception ex) {

                log.warn("Failed to load user or validate token for {}: {}", username, ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}