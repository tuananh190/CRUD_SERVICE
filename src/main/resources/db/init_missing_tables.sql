-- ============================================================
-- CRUD-SERVICE - Mạng xã hội (Back-end)
-- Script tạo 5 bảng còn thiếu cho database: crud-service
--
-- 3 bảng đã có:  user | posts | comments
-- 5 bảng cần tạo bên dưới
-- ============================================================

USE `crud-service`;

-- ============================================================
-- BẢNG 4: likes
-- Mối quan hệ:
--   user  (1) ──────<  likes  (Nhiều)
--   posts (1) ──────<  likes  (Nhiều)
--   comments (1) ───<  likes  (Nhiều)
-- Unique: 1 user chỉ được like 1 post / 1 comment đúng 1 lần
-- ============================================================
CREATE TABLE IF NOT EXISTS `likes` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL,
    `post_id`    BIGINT       NULL,
    `comment_id` BIGINT       NULL,
    `created_at` DATETIME(6)  NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_like_user_post`    (`user_id`, `post_id`),
    UNIQUE KEY `UK_like_user_comment` (`user_id`, `comment_id`),
    CONSTRAINT `FK_like_user`    FOREIGN KEY (`user_id`)    REFERENCES `user`     (`id`),
    CONSTRAINT `FK_like_post`    FOREIGN KEY (`post_id`)    REFERENCES `posts`    (`id`),
    CONSTRAINT `FK_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`)
);

-- ============================================================
-- BẢNG 5: follows
-- Mối quan hệ:
--   user (1) ──────< follows (Nhiều)  [vai follower]
--   user (1) ──────< follows (Nhiều)  [vai following]
-- Unique: không thể follow cùng 1 người 2 lần
-- ============================================================
CREATE TABLE IF NOT EXISTS `follows` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `follower_id`  BIGINT       NOT NULL,
    `following_id` BIGINT       NOT NULL,
    `created_at`   DATETIME(6)  NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_follow_pair` (`follower_id`, `following_id`),
    CONSTRAINT `FK_follow_follower`  FOREIGN KEY (`follower_id`)  REFERENCES `user` (`id`),
    CONSTRAINT `FK_follow_following` FOREIGN KEY (`following_id`) REFERENCES `user` (`id`)
);

-- ============================================================
-- BẢNG 6: notifications
-- Mối quan hệ:
--   user (1) ──────< notifications (Nhiều)
-- ============================================================
CREATE TABLE IF NOT EXISTS `notifications` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `recipient_id` BIGINT        NOT NULL,
    `message`      VARCHAR(500)  NOT NULL,
    `is_read`      BIT(1)        NOT NULL DEFAULT b'0',
    `created_at`   DATETIME(6)   NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `FK_notification_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `user` (`id`)
);

-- ============================================================
-- BẢNG 7: hashtags
-- Mối quan hệ:
--   posts >────────< hashtags  (Many-to-Many qua post_hashtags)
-- ============================================================
CREATE TABLE IF NOT EXISTS `hashtags` (
    `id`   BIGINT        NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100)  NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_hashtag_name` (`name`)
);

-- ============================================================
-- BẢNG 8: post_hashtags  (bảng trung gian Many-to-Many)
-- Liên kết posts >────────< hashtags
-- ============================================================
CREATE TABLE IF NOT EXISTS `post_hashtags` (
    `post_id`    BIGINT NOT NULL,
    `hashtag_id` BIGINT NOT NULL,
    PRIMARY KEY (`post_id`, `hashtag_id`),
    CONSTRAINT `FK_ph_post`    FOREIGN KEY (`post_id`)    REFERENCES `posts`    (`id`),
    CONSTRAINT `FK_ph_hashtag` FOREIGN KEY (`hashtag_id`) REFERENCES `hashtags` (`id`)
);

-- ============================================================
-- KIỂM TRA: Xem danh sách bảng sau khi tạo (phải thấy đủ 8)
-- ============================================================
SHOW TABLES;
