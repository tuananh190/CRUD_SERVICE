package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.UserCreationRequest;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import com.mar.CRUD_SERVICE.dto.request.ChangePasswordRequest;
import com.mar.CRUD_SERVICE.dto.request.ForgotPasswordRequest; // Bổ sung
import com.mar.CRUD_SERVICE.dto.request.ResetPasswordRequest; // Bổ sung

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Dependency mới
    private final EmailService emailService;       // Dependency mới


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }



    public User createUser(UserCreationRequest request){
        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());


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
        if(request.getEmail() != null) user.setEmail(request.getEmail());


        if(request.getPassword() != null) user.setPassword(passwordEncoder.encode(request.getPassword()));

        if(request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if(request.getLastName() != null) user.setLastName(request.getLastName());
        if(request.getDob() != null) user.setDob(request.getDob().toString());

        return userRepository.save(user);
    }

    public void deleteUser(Long id){
        if(!userRepository.existsById(id)){
            throw new IllegalStateException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
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


    // 2. QUÊN MẬT KHẨU (FORGOT PASSWORD - Giai đoạn 1)
    @Transactional
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                // Thông báo chung chung để tránh rò rỉ thông tin
                .orElseThrow(() -> new RuntimeException("Nếu tài khoản tồn tại, liên kết đặt lại sẽ được gửi đến email."));

        // Sinh Token và thời gian hết hạn (15 phút)
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15);

        // Lưu Token vào DB
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiry(expiryTime);
        userRepository.save(user);

        // Gửi email
        emailService.sendResetPasswordEmail(user.getEmail(), resetToken);
    }

    // 3. ĐẶT LẠI MẬT KHẨU (RESET PASSWORD - Giai đoạn 2)
    @Transactional
    public void resetPassword(String token, ResetPasswordRequest request) {

        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token đặt lại mật khẩu không hợp lệ."));

        // Kiểm tra Token đã hết hạn chưa
        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            // Xóa Token đã hết hạn và báo lỗi
            user.setResetPasswordToken(null);
            user.setResetPasswordTokenExpiry(null);
            userRepository.save(user);
            throw new IllegalArgumentException("Token đặt lại mật khẩu đã hết hạn.");
        }

        // Mã hóa mật khẩu mới và cập nhật
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // Xóa Token và thời gian hết hạn (đã dùng xong)
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);

        userRepository.save(user);
    }
}