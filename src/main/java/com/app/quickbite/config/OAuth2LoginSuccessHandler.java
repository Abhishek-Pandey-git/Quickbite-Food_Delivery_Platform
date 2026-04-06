package com.app.quickbite.config;

import com.app.quickbite.auth.entity.AuthProvider;
import com.app.quickbite.auth.entity.User;
import com.app.quickbite.auth.repository.UserRepository;
import com.app.quickbite.auth.security.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * OAuth2 Login Success Handler
 * 
 * This handler is called after successful Google OAuth2 authentication.
 * It creates/updates the user in our database and redirects to frontend with JWT token.
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    // Frontend URL for redirect after successful OAuth
    private static final String FRONTEND_URL = "http://localhost:5173";

    public OAuth2LoginSuccessHandler(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        logger.info("OAuth2LoginSuccessHandler initialized");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        logger.info("OAuth2 authentication success - processing user");
        
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String googleId = oAuth2User.getAttribute("sub");
            
            logger.info("OAuth2 user info - email: {}, name: {}", email, name);
            
            // Find or create user
            User user = findOrCreateUser(email, name, googleId);
            
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            
            logger.info("JWT token generated for OAuth2 user: {}", email);
            
            // Redirect to frontend with token
            String redirectUrl = FRONTEND_URL + "/auth/callback?token=" + 
                URLEncoder.encode(token, StandardCharsets.UTF_8) +
                "&role=" + URLEncoder.encode(user.getRole(), StandardCharsets.UTF_8) +
                "&email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
            
            logger.info("Redirecting to frontend: {}", FRONTEND_URL + "/auth/callback");
            
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            
        } catch (Exception e) {
            logger.error("Error processing OAuth2 success: {}", e.getMessage());
            // Redirect to frontend with error
            String errorUrl = FRONTEND_URL + "/?error=" + 
                URLEncoder.encode("OAuth2 authentication failed: " + e.getMessage(), StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    private User findOrCreateUser(String email, String name, String googleId) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            // Check if this user was created with password auth
            if (user.getOauthProvider() == AuthProvider.NONE || user.getOauthProvider() == null) {
                // User exists with password auth - update to link Google account
                user.setOauthProvider(AuthProvider.GOOGLE);
                user.setOauthId(googleId);
                user.setEmailVerified(true); // Google verified the email
                logger.info("Linked Google account to existing user: {}", email);
                return userRepository.save(user);
            }
            
            // User exists with OAuth - just return
            logger.info("Existing OAuth user found: {}", email);
            return user;
        }
        
        // Create new user
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(name != null ? name : "Google User");
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password
        newUser.setPhoneNumber(""); // Will need to be updated later
        newUser.setRole("CUSTOMER"); // OAuth users are always customers
        newUser.setOauthProvider(AuthProvider.GOOGLE);
        newUser.setOauthId(googleId);
        newUser.setEmailVerified(true); // Google verified the email
        
        User savedUser = userRepository.save(newUser);
        logger.info("Created new OAuth user: {}", email);
        
        return savedUser;
    }
}
