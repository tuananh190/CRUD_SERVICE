package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.request.ChangePasswordRequest;
import com.mar.CRUD_SERVICE.dto.request.UserCreationRequest;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.service.UserService;
import com.mar.CRUD_SERVICE.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody UserCreationRequest request){
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Tạo tài khoản thành công", user));
    }

    @GetMapping
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody UserCreationRequest request, Principal principal){
        User targetUser = userService.getUserById(id)
                .orElseThrow(() -> new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("User not found"));
        
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (principal == null || (!targetUser.getUsername().equals(principal.getName()) && !isAdmin)) {
            throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền sửa thông tin người khác");
        }
        User updated = userService.updateUser(id, request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id, Principal principal){
        User targetUser = userService.getUserById(id)
                .orElseThrow(() -> new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("User not found"));
        
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (principal == null || (!targetUser.getUsername().equals(principal.getName()) && !isAdmin)) {
            throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền xóa tài khoản của người khác");
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa người dùng thành công", null));
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam("keyword") String keyword) {
        List<User> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody ChangePasswordRequest request, Principal principal) {
        String currentUsername = principal.getName();
        userService.changePassword(currentUsername, request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Mật khẩu đã được thay đổi thành công!", null));
    }
}