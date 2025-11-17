package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.request.AuthenticationRequest;
import com.mar.CRUD_SERVICE.dto.request.RegisterRequest;
import com.mar.CRUD_SERVICE.dto.request.AuthenticationResponse;
import com.mar.CRUD_SERVICE.dto.request.ForgotPasswordRequest;
import com.mar.CRUD_SERVICE.dto.request.ResetPasswordRequest;
import com.mar.CRUD_SERVICE.service.AuthenticationService;
import com.mar.CRUD_SERVICE.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;
    private final UserService userService;

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

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            userService.forgotPassword(request.getEmail());
            // Trả về OK ngay cả khi email không tồn tại để tránh dò tìm tài khoản
            return ResponseEntity.ok("Yêu cầu đặt lại mật khẩu đã được xử lý. Vui lòng kiểm tra email.");
        } catch (Exception ex) {
            log.error("Lỗi trong quá trình quên mật khẩu cho email {}: {}", request.getEmail(), ex.getMessage());
            // Trả về 500 hoặc 200/OK với thông báo chung (tùy thuộc vào chính sách bảo mật)
            return ResponseEntity.ok("Yêu cầu đặt lại mật khẩu đã được xử lý. Vui lòng kiểm tra email.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam("token") String token,
            @RequestBody ResetPasswordRequest request
    ) {
        try {
            userService.resetPassword(token, request);
            return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công! Bạn có thể đăng nhập ngay bây giờ.");
        } catch (IllegalArgumentException ex) {
            // Xử lý lỗi Token không hợp lệ hoặc hết hạn (do logic trong UserService ném ra)
            log.warn("Đặt lại mật khẩu không thành công do mã thông báo không hợp lệ: {}", token);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Lỗi không mong muốn trong quá trình đặt lại mật khẩu: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống khi đặt lại mật khẩu.");
        }
    }
}