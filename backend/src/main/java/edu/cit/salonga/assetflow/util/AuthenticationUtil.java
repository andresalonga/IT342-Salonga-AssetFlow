package edu.cit.salonga.assetflow.util;

import edu.cit.salonga.assetflow.entity.User;
import edu.cit.salonga.assetflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationUtil {

    @Autowired
    private UserRepository userRepository;

    public Long getCurrentUserId() {
        try {
            String email = getCurrentUserEmail();
            System.out.println("🔍 [AuthenticationUtil] Looking up user by email: " + email);
            
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                Long userId = user.get().getId();
                System.out.println("✅ [AuthenticationUtil] Found user ID: " + userId);
                return userId;
            } else {
                System.err.println("❌ [AuthenticationUtil] No user found with email: " + email);
                throw new RuntimeException("User not found in database: " + email);
            }
        } catch (Exception e) {
            System.err.println("❌ [AuthenticationUtil] Error getting user ID: " + e.getMessage());
            throw new RuntimeException("Unable to extract user ID from authentication: " + e.getMessage());
        }
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        return authentication.getName();
    }
}
