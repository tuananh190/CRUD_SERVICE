package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.UserCreationRequest;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.Comment;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.CommentRepository;
import com.mar.CRUD_SERVICE.dto.request.ChangePasswordRequest;
import com.mar.CRUD_SERVICE.dto.request.DirectResetPasswordRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserBlockService userBlockService;

    public UserService(UserRepository userRepository,
                       PostRepository postRepository,
                       CommentRepository commentRepository,
                       PasswordEncoder passwordEncoder,
                       UserBlockService userBlockService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
        this.userBlockService = userBlockService;
    }

    public User createUser(UserCreationRequest request){
        User user = new User();

        user.setUsername(request.getUsername());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDob(request.getDob() != null ? request.getDob().toString() : null);

        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id){
        Optional<User> opt = userRepository.findById(id);
        if (opt.isPresent()) {
            User target = opt.get();
            try {
                String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
                if (currentUsername != null && !currentUsername.equals("anonymousUser")) {
                    User currentUser = userRepository.findByUsername(currentUsername).orElse(null);

                    if (currentUser != null && userBlockService.isBlockedBetween(currentUser, target)) {
                        return Optional.empty();
                    }
                }
            } catch (Exception e) {

            }
        }
        return opt;
    }

    public User updateUser(Long id, UserCreationRequest request){
        Optional<User> opt = userRepository.findById(id);
        if(opt.isEmpty()){
            throw new IllegalStateException("User not found with id: " + id);
        }
        User user = opt.get();
        if(request.getUsername() != null) user.setUsername(request.getUsername());

        if(request.getPassword() != null) user.setPassword(passwordEncoder.encode(request.getPassword()));

        if(request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if(request.getLastName() != null) user.setLastName(request.getLastName());
        if(request.getDob() != null) user.setDob(request.getDob().toString());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + id));

        List<Post> postsTagged = postRepository.findByTaggedUsersContaining(user);
        for(Post p : postsTagged){
            p.getTaggedUsers().remove(user);
            postRepository.save(p);
        }

        List<Comment> commentsTagged = commentRepository.findByTaggedUsersContaining(user);
        for(Comment c : commentsTagged){
            c.getTaggedUsers().remove(user);
            commentRepository.save(c);
        }

        userRepository.delete(user);
    }

    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);

        List<User> results = userRepository.findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                keyword, keyword, keyword);

        if (currentUser != null) {
            results = results.stream()
                    .filter(u -> !userBlockService.isBlockedBetween(currentUser, u))
                    .collect(java.util.stream.Collectors.toList());
        }
        return results;
    }

    @Transactional
    public void changePassword(String currentUsername, ChangePasswordRequest request) {

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);
    }

    @Transactional
    public void resetPasswordDirect(DirectResetPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Thông tin tài khoản không chính xác."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);
    }

    @Transactional
    public User updateProfile(String username, Map<String, String> updates) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));

        if (updates.containsKey("bio")) {
            user.setBio(updates.get("bio"));
        }
        if (updates.containsKey("avatarUrl")) {
            user.setAvatarUrl(updates.get("avatarUrl"));
        }
        if (updates.containsKey("firstName")) {
            user.setFirstName(updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName(updates.get("lastName"));
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updatePrivacy(String username, boolean isPrivate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        user.setPrivate(isPrivate);
        return userRepository.save(user);
    }
}