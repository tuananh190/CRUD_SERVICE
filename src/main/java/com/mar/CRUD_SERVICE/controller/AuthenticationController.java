package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.request.AuthenticationRequest;
import com.mar.CRUD_SERVICE.dto.request.RegisterRequest;
import com.mar.CRUD_SERVICE.dto.request.AuthenticationResponse;
import com.mar.CRUD_SERVICE.dto.request.DirectResetPasswordRequest;
import com.mar.CRUD_SERVICE.service.AuthenticationService;
import com.mar.CRUD_SERVICE.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import io.jsonwebtoken.Claims;
import com.mar.CRUD_SERVICE.service.TokenBlacklistService;
import com.mar.CRUD_SERVICE.service.JwtService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    public AuthenticationController(AuthenticationService authenticationService, 
                                    UserService userService,
                                    TokenBlacklistService tokenBlacklistService,
                                    JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtService = jwtService;
    }

    // API 1: Đăng ký tài khoản mới vào hệ thống
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request
    ) {
        try {
            AuthenticationResponse response = authenticationService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error during register: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    // API: Đăng ký tài khoản Admin mới (phục vụ mục đích test trên Postman)
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(
            @RequestBody RegisterRequest request
    ) {
        try {
            AuthenticationResponse response = authenticationService.registerAdmin(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error during admin register: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    // API 2: Đăng nhập và nhận JWT Token để xác thực
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        try {
            AuthenticationResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for user {}: {}", request.getUsername(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (Exception ex) {
            log.error("Unexpected error during authentication: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication error");
        }
    }


    // API 4.1: Đặt lại mật khẩu nhanh (Không dùng Email)
    @PostMapping("/reset-password-direct")
    public ResponseEntity<?> resetPasswordDirect(@RequestBody DirectResetPasswordRequest request) {
        try {
            userService.resetPasswordDirect(request);
            return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công! Bạn có thể đăng nhập bằng mật khẩu mới.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Lỗi khi đặt lại mật khẩu nhanh: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống.");
        }
    }

    // API 5: Đăng xuất (Logout) - Stateful JWT Blacklisting
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                // Trích xuất ngày hết hạn thực tế của token để lưu trữ kèm theo
                Date expiryDate = jwtService.extractClaim(jwt, Claims::getExpiration);
                
                // Đẩy token vào blacklist
                tokenBlacklistService.blacklistToken(jwt, expiryDate);
                log.info("Token added to blacklist on logout");
            } catch (Exception e) {
                log.warn("Lỗi khi xử lý token trong lúc logout: {}", e.getMessage());
                // Kể cả có lỗi parse JWT, vẫn trả về OK để user cảm thấy đã đăng xuất
            }
        }
        
        return ResponseEntity.ok("Đăng xuất thành công. Token đã bị vô hiệu hóa trên server.");
    }
}