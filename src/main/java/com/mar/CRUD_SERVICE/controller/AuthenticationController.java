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
import com.mar.CRUD_SERVICE.dto.response.ApiResponse;

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

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @RequestBody RegisterRequest request
    ) {
        AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đăng ký thành công", response));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> registerAdmin(
            @RequestBody RegisterRequest request
    ) {
        AuthenticationResponse response = authenticationService.registerAdmin(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đăng ký admin thành công", response));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đăng nhập thành công", response));
    }

    @PostMapping("/reset-password-direct")
    public ResponseEntity<ApiResponse<String>> resetPasswordDirect(@RequestBody DirectResetPasswordRequest request) {
        userService.resetPasswordDirect(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Mật khẩu đã được đặt lại thành công! Bạn có thể đăng nhập bằng mật khẩu mới.", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            Date expiryDate = jwtService.extractClaim(jwt, Claims::getExpiration);
            tokenBlacklistService.blacklistToken(jwt, expiryDate);
            log.info("Token added to blacklist on logout");
        }

        return ResponseEntity.ok(new ApiResponse<>(200, "Đăng xuất thành công. Token đã bị vô hiệu hóa trên server.", null));
    }
}