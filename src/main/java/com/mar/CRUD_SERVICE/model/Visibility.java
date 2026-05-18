package com.mar.CRUD_SERVICE.model;

/**
 * Quyền hiển thị của bài viết.
 * PUBLIC      → Ai cũng thấy (kể cả chưa đăng nhập)
 * FRIENDS_ONLY → Chỉ bạn bè (đã ACCEPTED) mới xem được
 */
public enum Visibility {
    PUBLIC,
    FRIENDS_ONLY
}
