package com.app.quickbite.auth.security;

import com.app.quickbite.auth.entity.User;
import com.app.quickbite.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CUSTOM USER DETAILS SERVICE - Load user from database with role-based authorities
 * 
 * This service is required by Spring Security to load user information during authentication.
 * By creating it as a separate @Service, we avoid circular dependency issues.
 * 
 * HOW IT'S USED:
 * 1. User tries to login with email + password
 * 2. Spring Security calls loadUserByUsername() with the email
 * 3. We load the user from database
 * 4. Spring Security compares the provided password with the stored hash
 * 5. If they match, authentication succeeds
 * 
 * IMPORTANT: This implementation includes the user's role as a GrantedAuthority,
 * which enables role-based access control (e.g., @PreAuthorize("hasRole('ADMIN')"))
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Load user by username (email in our case) with authorities
     * 
     * @param username The email address of the user
     * @return UserDetails Spring Security's representation of the user with role-based authorities
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user details for username: {}", username);
        
        try {
            // Load user from database by email
            User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            
            logger.info("User found in database: {} with role: {}", username, user.getRole());
            
            // Convert the user's role to a Spring Security GrantedAuthority
            // Spring Security convention is to prefix roles with "ROLE_"
            // Examples: ROLE_CUSTOMER, ROLE_RESTAURANT_OWNER, ROLE_DELIVERY_AGENT, ROLE_ADMIN
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
            logger.debug("Assigned authority: {} for user: {}", authority.getAuthority(), username);
            
            // Convert our User entity to Spring Security's UserDetails format
            // UserDetails is an interface that Spring Security understands
            return new org.springframework.security.core.userdetails.User(
                user.getEmail(),           // Username (email)
                user.getPassword(),        // Password hash (BCrypt encoded)
                List.of(authority)         // Authorities: user's role for role-based access control
            );
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found: {}", username);
            throw e;
        } catch (Exception e) {
            logger.error("Error loading user details for {}: {}", username, e.getMessage());
            throw e;
        }
    }
}
