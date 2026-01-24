package com.example.ecommerce.service;

import com.example.ecommerce.dto.AuthResponse;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.UnauthorizedException;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        
        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);
        
        user = userRepository.save(user);
        
        // Generate token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }
    
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        
        // Generate token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }
    
    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    public User makeAdmin(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("User not found with email: " + email));
        
        user.setRole(User.Role.ADMIN);
        return userRepository.save(user);
    }
}
