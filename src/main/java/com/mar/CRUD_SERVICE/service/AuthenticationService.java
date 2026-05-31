package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.AuthenticationRequest;
import com.mar.CRUD_SERVICE.dto.request.RegisterRequest;
import com.mar.CRUD_SERVICE.dto.request.AuthenticationResponse;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.Role;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {

    private static final String USERNAME_REGEX = "^(?=.*[0-9])[A-Z][a-zA-Z0-9]{9}$";

    private static final String PASSWORD_REGEX = "^(?=.*[0-9])[A-Z][a-zA-Z0-9]{9}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }

        if (!username.matches(USERNAME_REGEX)) {
            throw new IllegalArgumentException("Tên đăng nhập phải có ĐÚNG 10 ký tự, bao gồm cả chữ và số, trong đó chữ cái ĐẦU TIÊN bắt buộc viết hoa (vd: Tentoi2026).");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập '" + username + "' đã tồn tại. Vui lòng chọn tên khác.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }

        if (!password.matches(PASSWORD_REGEX)) {
            throw new IllegalArgumentException("Mật khẩu phải có ĐÚNG 10 ký tự, bao gồm cả chữ và số, trong đó chữ cái ĐẦU TIÊN bắt buộc viết hoa (vd: Bankeo2025).");
        }

        boolean passwordAlreadyUsed = userRepository.findAll().stream()
                .anyMatch(u -> passwordEncoder.matches(password, u.getPassword()));
        if (passwordAlreadyUsed) {
            throw new IllegalArgumentException("Mật khẩu này đã được sử dụng bởi tài khoản khác. Vui lòng chọn mật khẩu khác.");
        }

        var user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .dob(request.getDob() != null ? request.getDob().toString() : null)
                .role(Role.USER)
                .build();
        userRepository.save(user);

        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tokenVersion", user.getTokenVersion());
        var jwtToken = jwtService.generateToken(extraClaims, userDetails);

        return new AuthenticationResponse(jwtToken, true);
    }

    public AuthenticationResponse registerAdmin(RegisterRequest request) {

        if (userRepository.existsByRole(Role.ADMIN)) {
            throw new IllegalStateException("Hệ thống đã có 1 tài khoản Admin. Không được phép tạo thêm.");
        }

        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (!username.matches(USERNAME_REGEX)) {
            throw new IllegalArgumentException("Tên đăng nhập phải có ĐÚNG 10 ký tự, bao gồm cả chữ và số, trong đó chữ cái ĐẦU TIÊN bắt buộc viết hoa (vd: Tentoi2026).");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập '" + username + "' đã tồn tại. Vui lòng chọn tên khác.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        if (!password.matches(PASSWORD_REGEX)) {
            throw new IllegalArgumentException("Mật khẩu phải có ĐÚNG 10 ký tự, bao gồm cả chữ và số, trong đó chữ cái ĐẦU TIÊN bắt buộc viết hoa (vd: Bankeo2025).");
        }

        boolean passwordAlreadyUsed = userRepository.findAll().stream()
                .anyMatch(u -> passwordEncoder.matches(password, u.getPassword()));
        if (passwordAlreadyUsed) {
            throw new IllegalArgumentException("Mật khẩu này đã được sử dụng bởi tài khoản khác. Vui lòng chọn mật khẩu khác.");
        }

        var adminUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .dob(request.getDob() != null ? request.getDob().toString() : null)
                .role(Role.ADMIN)
                .build();
        userRepository.save(adminUser);

        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(adminUser.getUsername())
                .password(adminUser.getPassword())
                .authorities("ROLE_ADMIN")
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tokenVersion", adminUser.getTokenVersion());
        var jwtToken = jwtService.generateToken(extraClaims, userDetails);

        return new AuthenticationResponse(jwtToken, true);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String username = request.getUsername();

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với tên đăng nhập: " + username));

        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tokenVersion", user.getTokenVersion());
        var jwtToken = jwtService.generateToken(extraClaims, userDetails);

        return new AuthenticationResponse(jwtToken, true);
    }
}
