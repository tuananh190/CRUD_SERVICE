package com.mar.CRUD_SERVICE.config;

import com.mar.CRUD_SERVICE.service.JwtAuthenticationFilter;
import com.mar.CRUD_SERVICE.service.UserDetailsServiceImpl;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new UserDetailsServiceImpl(userRepository);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthFilter,
            AuthenticationProvider authenticationProvider
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // 🔒 ADMIN ONLY - reset password người khác (phải đặt TRƯỚC rule /api/v1/auth/**)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/reset-password-direct").hasRole("ADMIN")

                        // ✅ PUBLIC - không cần token
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // 📄 Swagger UI - không cần token (để xem tài liệu API)
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 🌐 ĐỌC BÀI VIẾT - không cần đăng nhập (bài viết là public)
                        // Chỉ cho phép GET (xem) — POST/PUT/DELETE vẫn cần token
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/trending").permitAll()

                        // 🌐 ĐỌC BÌNH LUẬN - không cần đăng nhập
                        .requestMatchers(HttpMethod.GET, "/api/v1/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/comments/{id}").permitAll()

                        // 🌐 XEM HASHTAG - không cần đăng nhập
                        .requestMatchers(HttpMethod.GET, "/api/v1/hashtags").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/hashtags/**").permitAll()

                        // 🌐 TÌM KIẾM - không cần đăng nhập
                        .requestMatchers(HttpMethod.GET, "/api/v1/search/**").permitAll()

                        // 👤 User tự đổi mật khẩu (đặt TRƯỚC /users/**)
                        .requestMatchers(HttpMethod.PUT, "/users/change-password").authenticated()

                        // 👑 ADMIN only
                        .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/{id}").hasRole("ADMIN")

                        // 👑 ADMIN only — Content Moderation Dashboard
                        // Đặt trước anyRequest() để Spring Security ưu tiên rule này
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Tất cả còn lại cần đăng nhập
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
