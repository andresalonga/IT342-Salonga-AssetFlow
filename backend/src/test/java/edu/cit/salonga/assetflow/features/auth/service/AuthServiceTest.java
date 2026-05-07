package edu.cit.salonga.assetflow.features.auth.service;

import edu.cit.salonga.assetflow.features.auth.dto.AuthResponse;
import edu.cit.salonga.assetflow.features.auth.dto.LoginRequest;
import edu.cit.salonga.assetflow.features.auth.dto.RegisterRequest;
import edu.cit.salonga.assetflow.features.auth.entity.Role;
import edu.cit.salonga.assetflow.features.auth.entity.User;
import edu.cit.salonga.assetflow.features.auth.repository.UserRepository;
import edu.cit.salonga.assetflow.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for AuthService
 * Tests authentication logic: registration, login, JWT generation, password hashing
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("student@test.edu");
        registerRequest.setPassword("SecureP@ss123");
        registerRequest.setName("John Student");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("student@test.edu");
        loginRequest.setPassword("SecureP@ss123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("student@test.edu");
        testUser.setName("John Student");
        testUser.setPassword(encoder.encode("SecureP@ss123"));
        testUser.setRole(Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken("student@test.edu", "USER")).thenReturn("jwt-token-123");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("student@test.edu", response.getEmail());
        assertEquals("USER", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateEmail() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Email already registered", response.getMessage());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken("student@test.edu", "USER")).thenReturn("jwt-token-456");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("jwt-token-456", response.getToken());
        assertEquals("student@test.edu", response.getEmail());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
    }

    @Test
    void testLoginUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
    }

    @Test
    void testLoginWrongPassword() {
        // Arrange
        LoginRequest wrongPasswordRequest = new LoginRequest();
        wrongPasswordRequest.setEmail("student@test.edu");
        wrongPasswordRequest.setPassword("WrongPassword123");
        
        when(userRepository.findByEmail(wrongPasswordRequest.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authService.login(wrongPasswordRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid credentials", response.getMessage());
    }

    @Test
    void testPasswordIsHashedOnRegister() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // Verify password is hashed (not plain text)
            assertNotEquals("SecureP@ss123", user.getPassword());
            assertTrue(encoder.matches("SecureP@ss123", user.getPassword()));
            return user;
        });
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        // Act
        authService.register(registerRequest);

        // Assert
        verify(userRepository, times(1)).save(argThat(user -> 
            encoder.matches("SecureP@ss123", user.getPassword())
        ));
    }

    @Test
    void testAdminRoleAssignmentForAdminEmail() {
        // Arrange - Email ends with "admin.com" should get ADMIN role
        RegisterRequest adminRegister = new RegisterRequest();
        adminRegister.setEmail("admin@admin.com");
        adminRegister.setPassword("AdminPass123");
        adminRegister.setName("Admin User");

        when(userRepository.existsByEmail(adminRegister.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals(Role.ADMIN, user.getRole(), "Admin email should get ADMIN role");
            return user;
        });
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("admin-jwt");

        // Act
        authService.register(adminRegister);

        // Assert
        verify(userRepository, times(1)).save(argThat(user -> 
            user.getRole() == Role.ADMIN
        ));
    }

}
