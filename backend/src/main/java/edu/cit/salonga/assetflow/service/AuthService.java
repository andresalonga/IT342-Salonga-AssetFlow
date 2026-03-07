package edu.cit.salonga.assetflow.service;

import edu.cit.salonga.assetflow.dto.AuthResponse;
import edu.cit.salonga.assetflow.dto.LoginRequest;
import edu.cit.salonga.assetflow.dto.RegisterRequest;
import edu.cit.salonga.assetflow.entity.User;
import edu.cit.salonga.assetflow.repository.UserRepository;
import edu.cit.salonga.assetflow.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse("Email already registered", null, null, null, null, null, false);
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        
        // Hash the password securely
        user.setPassword(encoder.encode(request.getPassword()));

        // Save user to database
        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser, savedUser.getId(), savedUser.getRole().name());

        return new AuthResponse(
            "User registered successfully",
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getRole().name(),
            token,
            true
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        
        if (user == null) {
            return new AuthResponse("User not found", null, null, null, null, null, false);
        }

        // Verify password
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthResponse("Invalid credentials", null, null, null, null, null, false);
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user, user.getId(), user.getRole().name());

        return new AuthResponse(
            "Login successful",
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            token,
            true
        );
    }
}
