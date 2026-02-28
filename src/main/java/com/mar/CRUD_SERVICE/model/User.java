package com.mar.CRUD_SERVICE.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    @Column(nullable = true, length = 500)
    private String bio;

    @Column(name = "avatar_url", nullable = true)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry;

    public User() {}

    public User(Long id, String username, String email, String password, String firstName, String lastName, String dob, Role role, String resetPasswordToken, LocalDateTime resetPasswordTokenExpiry) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.role = role;
        this.resetPasswordToken = resetPasswordToken;
        this.resetPasswordTokenExpiry = resetPasswordTokenExpiry;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getResetPasswordToken() { return resetPasswordToken; }
    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }

    public LocalDateTime getResetPasswordTokenExpiry() { return resetPasswordTokenExpiry; }
    public void setResetPasswordTokenExpiry(LocalDateTime resetPasswordTokenExpiry) { this.resetPasswordTokenExpiry = resetPasswordTokenExpiry; }

    // Simple builder (keeps usage in AuthenticationService unchanged)
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String dob;
        private Role role;
        private String resetPasswordToken;
        private LocalDateTime resetPasswordTokenExpiry;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder dob(String dob) { this.dob = dob; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder resetPasswordToken(String token) { this.resetPasswordToken = token; return this; }
        public Builder resetPasswordTokenExpiry(LocalDateTime expiry) { this.resetPasswordTokenExpiry = expiry; return this; }

        public User build() {
            return new User(id, username, email, password, firstName, lastName, dob, role, resetPasswordToken, resetPasswordTokenExpiry);
        }
    }
}