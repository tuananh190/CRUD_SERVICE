package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.model.TokenBlacklist;
import com.mar.CRUD_SERVICE.repository.TokenBlacklistRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    
    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    /**
     * Lưu JWT Token vào Blacklist cùng với thời gian hết hạn của nó.
     * Khi token thực sự hết hạn, ta không cần giữ nó trong database nữa.
     */
    public void blacklistToken(String token, Date expiryDate) {
        // Tránh lưu duplicate nếu user bấm logout 2 lần cùng 1 token
        if (!tokenBlacklistRepository.existsByToken(token)) {
            TokenBlacklist blacklist = new TokenBlacklist(token, expiryDate);
            tokenBlacklistRepository.save(blacklist);
        }
    }

    /**
     * Kiểm tra xem Token có nằm trong Blacklist hay không.
     * Độ phức tạp O(log n) nhờ index ở Database.
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
}
