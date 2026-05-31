package com.mar.CRUD_SERVICE.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(
    name = "token_blacklist",
    indexes = {

        @Index(name = "idx_token", columnList = "token", unique = true)
    }
)
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 1000)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;

    public TokenBlacklist() {}

    public TokenBlacklist(String token, Date expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
}
