import sys

def replace_in_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # PostService.java replacements
    content = content.replace('new IllegalStateException("Bài viết này chỉ dành cho bạn bè hoặc trang cá nhân đã bị khóa.")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bài viết này chỉ dành cho bạn bè hoặc trang cá nhân đã bị khóa.")')
    content = content.replace('new IllegalStateException(\n                            "Bài viết không tồn tại")', 'new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException(\n                            "Bài viết không tồn tại")')
    content = content.replace('new IllegalStateException("Bài viết không tồn tại")', 'new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Bài viết không tồn tại")')
    content = content.replace('new IllegalStateException("Bạn không có quyền chia sẻ bài viết này.")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền chia sẻ bài viết này.")')
    content = content.replace('new IllegalStateException("Hết phiên đăng nhập.")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Hết phiên đăng nhập.")')
    content = content.replace('new IllegalStateException(\n                            "Hết phiên đăng nhập.")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException(\n                            "Hết phiên đăng nhập.")')
    content = content.replace('new IllegalStateException(\n                        "Hết phiên đăng nhập.")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException(\n                        "Hết phiên đăng nhập.")')
    content = content.replace('new IllegalStateException("Bạn không thể chia sẻ bài viết của chính mình.")', 'new IllegalArgumentException("Bạn không thể chia sẻ bài viết của chính mình.")')
    content = content.replace('new IllegalStateException("Hết phiên đăng nhập hoặc chưa đăng nhập hợp lệ.")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Hết phiên đăng nhập hoặc chưa đăng nhập hợp lệ.")')
    content = content.replace('new IllegalStateException(\n                    "Bạn không có quyền sửa bài viết của người khác!")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException(\n                    "Bạn không có quyền sửa bài viết của người khác!")')
    content = content.replace('new IllegalStateException("Bạn không có quyền sửa bài viết của người khác!")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền sửa bài viết của người khác!")')
    content = content.replace('new IllegalStateException("Bạn không có quyền xóa bài viết của người khác!")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền xóa bài viết của người khác!")')

    # CommentServiceImpl.java replacements
    content = content.replace('new IllegalStateException("postId is required")', 'new IllegalArgumentException("postId is required")')
    content = content.replace('new IllegalStateException("Post not found with id=" + request.getPostId())', 'new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Post not found with id=" + request.getPostId())')
    content = content.replace('new IllegalStateException("Unauthenticated: cannot determine author")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Unauthenticated: cannot determine author")')
    content = content.replace('new IllegalStateException("Cannot determine username from authentication principal")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Cannot determine username from authentication principal")')
    content = content.replace('new IllegalStateException("Author user not found with username=" + uname)', 'new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Author user not found with username=" + uname)')
    content = content.replace('new IllegalStateException("Bạn không có quyền bình luận trên bài viết này.")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền bình luận trên bài viết này.")')
    content = content.replace('new IllegalStateException("Bạn chưa đăng nhập.")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn chưa đăng nhập.")')
    content = content.replace('new IllegalStateException("Không xác định được người dùng hiện tại.")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Không xác định được người dùng hiện tại.")')
    content = content.replace('new IllegalStateException("Bình luận không tồn tại với id=" + id)', 'new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Bình luận không tồn tại với id=" + id)')
    content = content.replace('new IllegalStateException("Bạn không có quyền sửa bình luận của người khác!")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền sửa bình luận của người khác!")')
    content = content.replace('new IllegalStateException("Bạn không có quyền xóa bình luận của người khác!")', 'new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền xóa bình luận của người khác!")')

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

replace_in_file("src/main/java/com/mar/CRUD_SERVICE/service/PostService.java")
replace_in_file("src/main/java/com/mar/CRUD_SERVICE/service/CommentServiceImpl.java")
