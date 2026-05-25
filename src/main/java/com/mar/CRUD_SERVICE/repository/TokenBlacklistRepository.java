package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    
    // Kiểm tra O(log n) nhờ index đã thiết lập trong entity
    boolean existsByToken(String token);
    
    // Phục vụ cho Cronjob dọn dẹp các token đã quá hạn (nếu cần triển khai sau này)
    void deleteByExpiryDateBefore(Date now);
}
