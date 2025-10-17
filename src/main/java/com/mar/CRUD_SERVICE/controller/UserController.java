package com.mar.CRUD_SERVICE.controller;
import com.mar.CRUD_SERVICE.dto.request.UserCreationRequest;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // Constructor injection - preferred and removes 'field not assigned' warnings in IDE
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(@RequestBody UserCreationRequest request){
        return userService.createUser(request);
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
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserCreationRequest request){
        try{
            User updated = userService.updateUser(id, request);
            return ResponseEntity.ok(updated);
        }catch(IllegalStateException e){
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        try{
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        }catch(IllegalStateException e){
            return ResponseEntity.notFound().build();
        }
    }
}