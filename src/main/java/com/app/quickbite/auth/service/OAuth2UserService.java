package com.app.quickbite.auth.service;

import com.app.quickbite.auth.dto.GoogleUserInfo;
import com.app.quickbite.auth.dto.OAuth2TokenRequest;
import com.app.quickbite.auth.dto.OAuth2TokenResponse;
import com.app.quickbite.auth.dto.OAuth2AuthResponse;
import com.app.quickbite.auth.entity.AuthProvider;
import com.app.quickbite.auth.entity.User;
import com.app.quickbite.auth.repository.UserRepository;
import com.app.quickbite.auth.security.JwtUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * OAuth2 User Service
 * Handles Google OAuth2 token exchange and user creation/login
 */
@Service
public class OAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserService.class);

   
    private UserRepository userRepository;

   
    private JwtUtil jwtUtil;

    
    private PasswordEncoder passwordEncoder;

  
    private RestTemplate restTemplate;

    public OAuth2UserService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, RestTemplate restTemplate) {
        logger.info("Initializing OAuth2UserService with UserRepository, JwtUtil, PasswordEncoder, and RestTemplate");
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
    }

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_ENDPOINT = "https://openidconnect.googleapis.com/v1/userinfo";

    /**
     * Process OAuth2 login - exchange code for token, get user info, create/update user
     */
    public OAuth2AuthResponse authenticateWithGoogle(OAuth2TokenRequest request) {
        logger.info("Starting Google OAuth2 authentication");

        // Step 1: Exchange auth code for access token
        OAuth2TokenResponse tokenResponse = exchangeCodeForToken(request.getCode(), request.getRedirectUri());
        logger.debug("Successfully exchanged code for access token");

        // Step 2: Get user info from Google
        GoogleUserInfo googleUserInfo = getUserInfo(tokenResponse.getAccessToken());
        logger.info("Retrieved user info from Google for email: {}", googleUserInfo.getEmail());

        // Step 3: Find or create user
        User user = findOrCreateUser(googleUserInfo);

        // Step 4: Generate JWT token
        String jwtToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        logger.info("Generated JWT token for user: {}", user.getEmail());

        return new OAuth2AuthResponse(
            jwtToken,
            user.getRole(),
            user.getEmail(),
            "Google login successful"
        );
    }

    /**
     * Exchange authorization code for access token
     */
    private OAuth2TokenResponse exchangeCodeForToken(String code, String redirectUri) {
        logger.debug("Exchanging authorization code for access token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<OAuth2TokenResponse> response = restTemplate.postForEntity(
            TOKEN_ENDPOINT,
            request,
            OAuth2TokenResponse.class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            logger.error("Failed to exchange code for token");
            throw new RuntimeException("Failed to exchange authorization code for token");
        }

        return response.getBody();
    }

    /**
     * Get user information from Google using access token
     */
    private GoogleUserInfo getUserInfo(String accessToken) {
        logger.debug("Fetching user info from Google");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
            USERINFO_ENDPOINT,
            HttpMethod.GET,
            request,
            GoogleUserInfo.class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            logger.error("Failed to fetch user info from Google");
            throw new RuntimeException("Failed to fetch user information from Google");
        }

        return response.getBody();
    }

    /**
     * Find existing OAuth user or create new one
     */
    private User findOrCreateUser(GoogleUserInfo googleUserInfo) {
        String email = googleUserInfo.getEmail();
        String oauthId = googleUserInfo.getSub();

        // Check if user already exists with this OAuth ID
        Optional<User> existingOAuthUser = userRepository.findByOauthProviderAndOauthId(
            AuthProvider.GOOGLE,
            oauthId
        );

        if (existingOAuthUser.isPresent()) {
            logger.info("Existing OAuth user found: {}", email);
            return existingOAuthUser.get();
        }

        // Check if email already exists with different auth method
        Optional<User> existingEmailUser = userRepository.findByEmail(email);
        if (existingEmailUser.isPresent()) {
            User user = existingEmailUser.get();
            if (user.getOauthProvider() == null || user.getOauthProvider() == AuthProvider.NONE) {
                logger.error("Email {} already registered with password authentication", email);
                throw new RuntimeException("Email already registered with password. Please use password login.");
            }
        }

        // Create new user
        logger.info("Creating new OAuth user: {}", email);
        return createOAuthUser(googleUserInfo);
    }

    /**
     * Create a new user from Google OAuth data
     */
    private User createOAuthUser(GoogleUserInfo googleUserInfo) {
        User user = new User();

        // Use name from Google, fallback to email if name is null/empty
        String fullName = googleUserInfo.getName();
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = googleUserInfo.getEmail();
            logger.debug("Name not provided by Google, using email as fullName");
        }

        user.setFullName(fullName);
        user.setEmail(googleUserInfo.getEmail());
        user.setRole("CUSTOMER");  // OAuth users are always customers
        user.setEmailVerified(true);  // Google-verified email
        user.setOauthProvider(AuthProvider.GOOGLE);
        user.setOauthId(googleUserInfo.getSub());

        // Set a random password (OAuth users don't use it)
        String randomPassword = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(randomPassword));

        // Phone number can be set later by user
        user.setPhoneNumber("");

        User savedUser = userRepository.save(user);
        logger.info("Successfully created OAuth user: {}", savedUser.getEmail());

        return savedUser;
    }
}