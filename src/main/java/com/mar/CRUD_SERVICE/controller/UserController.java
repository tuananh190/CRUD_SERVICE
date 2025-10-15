package com.mar.CRUD_SERVICE.controller;
import com.mar.CRUD_SERVICE.dto.request.UserCreationRequest;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/users")
    public User createUser(@RequestBody UserCreationRequest request){
        return userService.createUser(request);
    }
}