package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.AuthenticationRequest;
import com.mar.CRUD_SERVICE.dto.request.RegisterRequest;
import com.mar.CRUD_SERVICE.dto.request.AuthenticationResponse;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.Role;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
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
        var jwtToken = jwtService.generateToken(userDetails);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .authenticated(true)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String principal = request.getUsername() != null ? request.getUsername() : request.getEmail();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        request.getPassword()
                )
        );
        var user = userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow();
        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();
        var jwtToken = jwtService.generateToken(userDetails);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .authenticated(true)
                .build();
    }
}