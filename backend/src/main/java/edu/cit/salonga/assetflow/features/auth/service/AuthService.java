package edu.cit.salonga.assetflow.features.auth.service;

import edu.cit.salonga.assetflow.features.auth.dto.AuthResponse;
import edu.cit.salonga.assetflow.features.auth.dto.GoogleTokenResponse;
import edu.cit.salonga.assetflow.features.auth.dto.GoogleUserInfo;
import edu.cit.salonga.assetflow.features.auth.dto.LoginRequest;
import edu.cit.salonga.assetflow.features.auth.dto.RegisterRequest;
import edu.cit.salonga.assetflow.features.auth.entity.Role;
import edu.cit.salonga.assetflow.features.auth.entity.User;
import edu.cit.salonga.assetflow.features.auth.repository.UserRepository;
import edu.cit.salonga.assetflow.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${google.oauth.client-id}")
    private String googleClientId;

    @Value("${google.oauth.client-secret}")
    private String googleClientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String googleRedirectUri;

    @Value("${google.oauth.frontend-redirect}")
    private String googleFrontendRedirect;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final RestTemplate restTemplate = new RestTemplate();

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

        // Assign role based on email domain
        if (request.getEmail().contains("admin.com")) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }

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

    public String buildGoogleLoginUrl() {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new RuntimeException("Google OAuth client ID is not configured");
        }

        String scope = "openid email profile";
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + googleClientId +
                "&redirect_uri=" + googleRedirectUri +
                "&response_type=code" +
                "&scope=" + scope.replace(" ", "%20");
    }

    public String getFrontendRedirectUri() {
        return googleFrontendRedirect;
    }

    public AuthResponse handleGoogleCallback(String code) {
        GoogleTokenResponse tokenResponse = exchangeCodeForToken(code);
        GoogleUserInfo userInfo = fetchGoogleUserInfo(tokenResponse.getAccessToken());

        if (userInfo == null || userInfo.getEmail() == null) {
            return new AuthResponse("Failed to read Google profile", null, null, null, null, null, false);
        }

        User user = userRepository.findByGoogleId(userInfo.getSub())
                .orElseGet(() -> userRepository.findByEmail(userInfo.getEmail()).orElse(null));

        if (user == null) {
            user = new User();
            user.setName(userInfo.getName() != null ? userInfo.getName() : "Google User");
            user.setEmail(userInfo.getEmail());
            user.setGoogleId(userInfo.getSub());
            user.setPassword(encoder.encode(UUID.randomUUID().toString()));
            user.setRole(Role.USER);
            user = userRepository.save(user);
        } else if (user.getGoogleId() == null) {
            user.setGoogleId(userInfo.getSub());
            user = userRepository.save(user);
        }

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

    private GoogleTokenResponse exchangeCodeForToken(String code) {
        if (googleClientId == null || googleClientId.isBlank() || googleClientSecret == null || googleClientSecret.isBlank()) {
            throw new RuntimeException("Google OAuth client credentials are not configured");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("redirect_uri", googleRedirectUri);
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                request,
                GoogleTokenResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to exchange authorization code");
        }

        return response.getBody();
    }

    private GoogleUserInfo fetchGoogleUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                request,
                GoogleUserInfo.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to fetch Google user info");
        }

        return response.getBody();
    }
}
