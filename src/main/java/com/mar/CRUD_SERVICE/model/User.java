package com.mar.CRUD_SERVICE.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime; // Bổ sung import

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = true)
    private String dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // ****************************************************
    // BỔ SUNG: TRƯỜNG DÀNH CHO CHỨC NĂNG RESET MẬT KHẨU
    // ****************************************************

    // Lưu chuỗi token ngẫu nhiên (UUID)
    // Cho phép NULL vì token chỉ tồn tại khi người dùng yêu cầu đặt lại mật khẩu
    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    // Thời gian Token hết hạn
    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry; // Sử dụng LocalDateTime cho DB
}