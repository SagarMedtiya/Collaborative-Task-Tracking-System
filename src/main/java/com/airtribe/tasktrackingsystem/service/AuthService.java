package com.airtribe.tasktrackingsystem.service;

import com.airtribe.tasktrackingsystem.dto.request.AuthRequest;
import com.airtribe.tasktrackingsystem.dto.request.RegisterRequest;
import com.airtribe.tasktrackingsystem.dto.response.AuthResponse;
import com.airtribe.tasktrackingsystem.model.Users;
import com.airtribe.tasktrackingsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        //validate if uswername or email already exists
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new AuthenticationException("Username is already taken");
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email is already taken");
        }
        //Create new User
        Users user = Users.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Users.Role.USER)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        //Genrate token
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true,
                true,
                true,
                true,
                java.util.Collections.emptyList()
        );
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole().name(), user.getId());

    }

    public AuthResponse login(AuthRequest request){
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Users user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                    .orElseThrow(()-> new AuthenticationException("User not found"));

            String token = jwtService.generateToken(userDetails);
            return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole().name(), user.getId());
        }catch(Exception e){
            throw new AuthenticationException("Invalid username or password");
        }
    }
    public void logout(String token){
        // in a stateless JWT system, logout is handled client-side
        //We could implement a blacklist mechanism here if needed
    }
}
