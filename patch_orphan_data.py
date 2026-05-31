import sys
import re

def patch_post_service():
    filepath = "src/main/java/com/mar/CRUD_SERVICE/service/PostService.java"
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    target = """        notificationRepository.deleteAllByReferenceId(post.getId());
        reportRepository.deleteAllByTargetTypeAndTargetId("POST", post.getId());

        postRepository.deleteById(id);"""

    replacement = """        if (post.getComments() != null) {
            for (Comment c : post.getComments()) {
                reportRepository.deleteAllByTargetTypeAndTargetId("COMMENT", c.getId());
                notificationRepository.deleteAllByReferenceId(c.getId());
            }
        }
        notificationRepository.deleteAllByReferenceId(post.getId());
        reportRepository.deleteAllByTargetTypeAndTargetId("POST", post.getId());

        postRepository.deleteById(id);"""

    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

def patch_user_service():
    filepath = "src/main/java/com/mar/CRUD_SERVICE/service/UserService.java"
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Add imports
    import_target = "import com.mar.CRUD_SERVICE.repository.CommentRepository;"
    import_replacement = """import com.mar.CRUD_SERVICE.repository.CommentRepository;
import com.mar.CRUD_SERVICE.repository.ReportRepository;
import com.mar.CRUD_SERVICE.repository.NotificationRepository;"""
    content = content.replace(import_target, import_replacement)

    # 2. Add fields
    fields_target = """    private final PasswordEncoder passwordEncoder;
    private final UserBlockService userBlockService;"""
    fields_replacement = """    private final PasswordEncoder passwordEncoder;
    private final UserBlockService userBlockService;
    private final ReportRepository reportRepository;
    private final NotificationRepository notificationRepository;"""
    content = content.replace(fields_target, fields_replacement)

    # 3. Modify constructor
    constructor_target = """    public UserService(UserRepository userRepository,
                       PostRepository postRepository,
                       CommentRepository commentRepository,
                       PasswordEncoder passwordEncoder,
                       UserBlockService userBlockService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
        this.userBlockService = userBlockService;
    }"""
    constructor_replacement = """    public UserService(UserRepository userRepository,
                       PostRepository postRepository,
                       CommentRepository commentRepository,
                       PasswordEncoder passwordEncoder,
                       UserBlockService userBlockService,
                       ReportRepository reportRepository,
                       NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
        this.userBlockService = userBlockService;
        this.reportRepository = reportRepository;
        this.notificationRepository = notificationRepository;
    }"""
    content = content.replace(constructor_target, constructor_replacement)

    # 4. Modify deleteUser
    delete_target = """    @Transactional
    public void deleteUser(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + id));

        List<Post> postsTagged = postRepository.findByTaggedUsersContaining(user);"""
    
    delete_replacement = """    @Transactional
    public void deleteUser(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + id));

        reportRepository.deleteAllByTargetTypeAndTargetId("USER", user.getId());

        if (user.getPosts() != null) {
            for (Post p : user.getPosts()) {
                reportRepository.deleteAllByTargetTypeAndTargetId("POST", p.getId());
                notificationRepository.deleteAllByReferenceId(p.getId());
            }
        }

        if (user.getComments() != null) {
            for (Comment c : user.getComments()) {
                reportRepository.deleteAllByTargetTypeAndTargetId("COMMENT", c.getId());
                notificationRepository.deleteAllByReferenceId(c.getId());
            }
        }

        List<Post> postsTagged = postRepository.findByTaggedUsersContaining(user);"""
    
    content = content.replace(delete_target, delete_replacement)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

if __name__ == '__main__':
    patch_post_service()
    patch_user_service()
    print("Patch applied successfully.")
