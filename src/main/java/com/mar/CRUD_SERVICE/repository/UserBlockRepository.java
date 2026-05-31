package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    @Query("SELECT COUNT(b) > 0 FROM UserBlock b WHERE b.blocker = :blocker AND b.blocked = :blocked")
    boolean existsByBlockerAndBlocked(@Param("blocker") User blocker, @Param("blocked") User blocked);

    @Query("SELECT COUNT(b) > 0 FROM UserBlock b WHERE (b.blocker = :u1 AND b.blocked = :u2) OR (b.blocker = :u2 AND b.blocked = :u1)")
    boolean existsBlockBetween(@Param("u1") User u1, @Param("u2") User u2);

    @Query("SELECT b FROM UserBlock b WHERE b.blocker = :blocker AND b.blocked = :blocked")
    Optional<UserBlock> findByBlockerAndBlocked(@Param("blocker") User blocker, @Param("blocked") User blocked);

    @Query("SELECT b FROM UserBlock b WHERE b.blocker = :blocker ORDER BY b.createdAt DESC")
    List<UserBlock> findAllByBlocker(@Param("blocker") User blocker);

    @Query("SELECT b.blocked.id FROM UserBlock b WHERE b.blocker = :blocker")
    List<Long> findBlockedUserIdsByBlocker(@Param("blocker") User blocker);
}
