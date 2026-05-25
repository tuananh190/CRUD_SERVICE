package com.mar.CRUD_SERVICE.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entity lưu trữ danh sách các JWT token đã bị thu hồi (Blacklist) sau khi user logout.
 * 
 * Bản chất của JWT là stateless (không cần query DB để xác thực), nhưng khi user chủ động
 * logout, ta buộc phải biến nó thành stateful một phần bằng cách lưu token vào Blacklist
 * cho đến khi nó tự hết hạn (expiryDate).
 */
@Entity
@Table(
    name = "token_blacklist",
    indexes = {
        // Index trên cột token để kiểm tra exists() với độ phức tạp O(log n)
        @Index(name = "idx_token", columnList = "token", unique = true)
    }
)
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JWT token thường khá dài, nên set length lớn (ví dụ 500 hoặc 1000)
    @Column(nullable = false, unique = true, length = 1000)
    private String token;

    // Lưu lại thời gian hết hạn của token.
    // Khi token quá hạn, ta có thể chạy một cronjob xóa đi để giảm tải DB.
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
