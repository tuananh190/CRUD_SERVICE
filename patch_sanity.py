import sys

def patch_jwt_service():
    filepath = "src/main/java/com/mar/CRUD_SERVICE/service/JwtService.java"
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    target = ".setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))"
    repl = ".setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))"
    
    content = content.replace(target, repl)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

def patch_post_service():
    filepath = "src/main/java/com/mar/CRUD_SERVICE/service/PostService.java"
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    target = """    public PostResponse createPost(PostCreationRequest request) {
        Post post = new Post();"""
        
    repl = """    public PostResponse createPost(PostCreationRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung không được để trống");
        }
        Post post = new Post();"""
        
    content = content.replace(target, repl)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

def patch_comment_service():
    filepath = "src/main/java/com/mar/CRUD_SERVICE/service/CommentServiceImpl.java"
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
        
    target = """    public CommentResponse createComment(CommentCreationRequest request) {
        log.debug("createComment called with postId={} text={}", request.getPostId(), request.getText());"""
        
    repl = """    public CommentResponse createComment(CommentCreationRequest request) {
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung không được để trống");
        }
        log.debug("createComment called with postId={} text={}", request.getPostId(), request.getText());"""
        
    content = content.replace(target, repl)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

if __name__ == '__main__':
    patch_jwt_service()
    patch_post_service()
    patch_comment_service()
    print("Patches applied successfully.")
