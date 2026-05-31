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

    public void blacklistToken(String token, Date expiryDate) {

        if (!tokenBlacklistRepository.existsByToken(token)) {
            TokenBlacklist blacklist = new TokenBlacklist(token, expiryDate);
            tokenBlacklistRepository.save(blacklist);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
}
