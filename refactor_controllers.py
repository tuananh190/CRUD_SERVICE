import sys

def process_file(filepath, replacements):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    for old, new in replacements:
        content = content.replace(old, new)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

post_replacements = [
    (
"""        try {
            PostResponse resp = postService.createPost(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }""",
"""        PostResponse resp = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);"""
    ),
    (
"""        try {

            String currentUsername = null;
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            }
            if (currentUsername == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng chưa đăng nhập.");
            }

            PostResponse resp = postService.sharePost(id, request, currentUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }""",
"""        String currentUsername = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (currentUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng chưa đăng nhập.");
        }

        PostResponse resp = postService.sharePost(id, request, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);"""
    ),
    (
"""        try {
            PostResponse updated = postService.updatePost(id, request);
            if (updated == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }""",
"""        PostResponse updated = postService.updatePost(id, request);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);"""
    ),
    (
"""        try {
            postService.deletePost(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Xóa bài viết thành công", null));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }""",
"""        postService.deletePost(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa bài viết thành công", null));"""
    )
]

comment_replacements = [
    (
"""        try {
            CommentResponse resp = commentService.createComment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }""",
"""        CommentResponse resp = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);"""
    ),
    (
"""        try {
            CommentResponse updated = commentService.updateComment(id, request);
            if (updated == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }""",
"""        CommentResponse updated = commentService.updateComment(id, request);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);"""
    ),
    (
"""        try {
            commentService.deleteComment(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Xóa bình luận thành công", null));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }""",
"""        commentService.deleteComment(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa bình luận thành công", null));"""
    )
]

process_file("src/main/java/com/mar/CRUD_SERVICE/controller/PostController.java", post_replacements)
process_file("src/main/java/com/mar/CRUD_SERVICE/controller/CommentController.java", comment_replacements)
