package edu.cit.salonga.assetflow.features.auth.controller;

import edu.cit.salonga.assetflow.features.auth.dto.AuthResponse;
import edu.cit.salonga.assetflow.features.auth.dto.LoginRequest;
import edu.cit.salonga.assetflow.features.auth.dto.RegisterRequest;
import edu.cit.salonga.assetflow.features.auth.entity.User;
import edu.cit.salonga.assetflow.features.auth.repository.UserRepository;
import edu.cit.salonga.assetflow.features.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole().name());
        userData.put("createdAt", user.getCreatedAt());
        
        return ResponseEntity.ok(userData);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Since we're using JWT (stateless), logout is handled on the client side
        // by removing the token from storage
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        response.put("note", "Please remove the token from client storage");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/google/login")
    public ResponseEntity<Void> googleLogin() {
        String url = authService.buildGoogleLoginUrl();
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    @GetMapping("/google/callback")
    public ResponseEntity<Void> googleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error) {
        if (error != null) {
            URI errorRedirect = URI.create(buildRedirectUrl(Map.of("error", error)));
            return ResponseEntity.status(HttpStatus.FOUND).location(errorRedirect).build();
        }

        if (code == null || code.isBlank()) {
            URI errorRedirect = URI.create(buildRedirectUrl(Map.of("error", "Missing authorization code")));
            return ResponseEntity.status(HttpStatus.FOUND).location(errorRedirect).build();
        }

        AuthResponse response = authService.handleGoogleCallback(code);
        if (!response.isSuccess()) {
            URI errorRedirect = URI.create(buildRedirectUrl(Map.of("error", response.getMessage())));
            return ResponseEntity.status(HttpStatus.FOUND).location(errorRedirect).build();
        }

        URI redirect = URI.create(buildRedirectUrl(Map.of(
                "token", response.getToken(),
                "userId", String.valueOf(response.getUserId()),
                "name", response.getName(),
                "email", response.getEmail(),
                "role", response.getRole()
        )));

        return ResponseEntity.status(HttpStatus.FOUND).location(redirect).build();
    }

    private String buildRedirectUrl(Map<String, String> params) {
        String base = UriComponentsBuilder.fromUriString(authService.getFrontendRedirectUri())
                .build()
                .toUriString();

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (query.length() > 0) {
                query.append("&");
            }
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            query.append("=");
            query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        if (query.length() == 0) {
            return base;
        }

        String separator = base.contains("?") ? "&" : "?";
        return base + separator + query;
    }
}
