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
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder; // Dependency mới

    public UserService(UserRepository userRepository, 
                       PostRepository postRepository,
                       CommentRepository commentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
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
        return userRepository.findById(id);
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

        // 1. Gỡ tag của user khỏi các bài viết (để không bị lỗi khóa ngoại bảng post_tagged_users)
        List<Post> postsTagged = postRepository.findByTaggedUsersContaining(user);
        for(Post p : postsTagged){
            p.getTaggedUsers().remove(user);
            postRepository.save(p);
        }

        // 2. Gỡ tag của user khỏi các bình luận (để không bị lỗi khóa ngoại bảng comment_tagged_users)
        List<Comment> commentsTagged = commentRepository.findByTaggedUsersContaining(user);
        for(Comment c : commentsTagged){
            c.getTaggedUsers().remove(user);
            commentRepository.save(c);
        }

        // 3. Logic xử lý bảng shares (Trống do đã tách cấu trúc, Cascade sẽ do DB lo nếu cần)

        // Xóa hoàn toàn người dùng cùng toàn bộ Entity nằm trong CascadeType.ALL
        userRepository.delete(user);
    }

    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                keyword, keyword, keyword);
    }

    // 1. ĐỔI MẬT KHẨU (CHANGE PASSWORD)
    @Transactional
    public void changePassword(String currentUsername, ChangePasswordRequest request) {

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));

        // So sánh mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng.");
        }

        // Mã hóa và Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }



    // 4. ĐẶT LẠI MẬT KHẨU NHANH GỌN (DÀNH CHO ĐỒ ÁN ĐƠN GIẢN - KHÔNG CẦN EMAIL TOKEN)
    @Transactional
    public void resetPasswordDirect(DirectResetPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Thông tin tài khoản không chính xác."));

        // Đã gỡ bỏ kiểm tra Role.ADMIN. Admin được quyền đổi mật khẩu nhanh.

        // Đổi Mật Khẩu lập tức
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}