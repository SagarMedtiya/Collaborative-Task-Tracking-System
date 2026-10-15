package com.airtribe.tasktrackingsystem.service;

import com.airtribe.tasktrackingsystem.dto.request.RegisterRequest;
import com.airtribe.tasktrackingsystem.dto.response.UserResponse;
import com.airtribe.tasktrackingsystem.exception.ResourceNotFoundException;
import com.airtribe.tasktrackingsystem.model.Users;
import com.airtribe.tasktrackingsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserById(Long id){
        Users user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found with id: "+ id));
        return UserResponse.from(user);
    }
    public UserResponse getUserByUsername(String username){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with username: "+ username));
        return UserResponse.from(user);
    }
    public Page<UserResponse> getAllUsers(Pageable pageable){
        return userRepository.findAll(pageable)
                .map(UserResponse::from);
    }

    @Transactional
    public UserResponse updateUser(Long id, RegisterRequest request){
        Users user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with id:" + id));
        if(request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if(userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username is already taken");
            }
            user.setUsername(request.getUsername());
        }
        if(request.getEmail() != null && !request.getEmail().equals(user.getEmail())){
            if(userRepository.existsByEmail(request.getEmail())){
                throw new RuntimeException("Email is already registered");
            }
            user.setEmail(request.getEmail());
        }
        if(request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if(request.getLastName() != null){
            user.setLastName(request.getLastName());
        }
        if(request.getPassword() != null && !request.getPassword().isEmpty()){
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user = userRepository.save(user);
        return UserResponse.from(user);
    }
    public Users getEntityByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }
    @Transactional
    public void deleteUser(Long id){
        if(!userRepository.existsById(id)){
            throw new ResourceNotFoundException("User not found with id: "+ id);
        }
        userRepository.deleteById(id);
    }

    public Users getEntityById(Long id){
        return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with id: " + id));
    }
}
