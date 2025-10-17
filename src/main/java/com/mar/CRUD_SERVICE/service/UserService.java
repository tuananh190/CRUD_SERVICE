package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.UserCreationRequest;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(UserCreationRequest request){
        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
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
        if(request.getPassword() != null) user.setPassword(request.getPassword());
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

}
