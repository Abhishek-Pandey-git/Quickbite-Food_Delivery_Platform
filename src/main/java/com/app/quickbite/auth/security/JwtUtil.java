package com.app.quickbite.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT UTILITY CLASS - Token Generation and Validation
 * 
 * JWT (JSON Web Token) is a compact way to securely transmit information between parties.
 * In our case, after a user logs in successfully, we give them a JWT token.
 * They send this token with every subsequent request to prove they're authenticated.
 * 
 * A JWT has 3 parts (separated by dots):
 * 1. Header: Token type and algorithm (e.g., {"alg": "HS256", "typ": "JWT"})
 * 2. Payload: User data (claims) like email, role, expiry time
 * 3. Signature: Ensures the token hasn't been tampered with
 * 
 * Example token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIn0.abc123signature
 */
@Component // Makes this a Spring-managed bean so we can inject it into services
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    /**
     * Secret key used to sign and verify tokens
     * 
     * IMPORTANT: In a real production app, this should be:
     * 1. At least 256 bits (32 characters) for HS256 algorithm
     * 2. Stored in environment variables or a secure vault (NOT hardcoded)
     * 3. Different for each environment (dev, staging, production)
     * 
     * For this MVP, we're using a hardcoded secret key for simplicity.
     */
    private static final String SECRET_KEY = "QuickBite2026SecretKeyForJWTTokenGenerationAndValidation";
    
    /**
     * Token expiration time: 24 hours (in milliseconds)
     * 
     * After 24 hours, the token becomes invalid and the user must log in again.
     * For mobile apps, you might want 7 or 30 days. For banking, maybe 15 minutes.
     */
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours
    
    /**
     * Generate a JWT token for a user
     * 
     * This method is called after successful login. It creates a token containing:
     * - Subject: User's email (username)
     * - Issued At: Current timestamp
     * - Expiration: 24 hours from now
     * - Signature: Signed with our secret key
     * 
     * @param email The user's email address (used as the username)
     * @return String The generated JWT token
     */
    public String generateToken(String email, String role) {
    logger.debug("Generating JWT token for user: {} with role: {}", email, role);
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);  // Add role to claims
    String token = createToken(claims, email);
    logger.debug("JWT token generated successfully for user: {}", email);
    return token;
}
    
    /**
     * PRIVATE HELPER - Actually create the token
     * 
     * This is where the magic happens! We use the JJWT library to build the token.
     * 
     * @param claims Additional data to embed in the token (empty for now, but could include role, userId, etc.)
     * @param subject The user's email (becomes the "sub" claim in the token)
     * @return String The generated JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)                              // Add custom claims (empty for now)
                .subject(subject)                            // Set the subject (user email)
                .issuedAt(new Date())                        // Token creation timestamp
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Expiry time
                .signWith(getSigningKey())                   // Sign with our secret key
                .compact();                                  // Convert to string format
    }
    
    /**
     * Extract the username (email) from a token
     * 
     * This is used by the authentication filter to identify which user is making the request.
     * 
     * Example flow:
     * 1. User sends request with header: "Authorization: Bearer eyJhbGc..."
     * 2. Filter extracts the token and calls this method
     * 3. We decode the token and extract the "sub" (subject) claim
     * 4. Return the email address
     * 
     * @param token The JWT token
     * @return String The user's email address
     */
    public String extractUsername(String token) {
        try {
            logger.debug("Extracting username from JWT token");
            String username = extractAllClaims(token).getSubject();
            logger.debug("Successfully extracted username: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Failed to extract username from token: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Validate a token
     * 
     * A token is valid if:
     * 1. The username in the token matches the expected username
     * 2. The token has not expired
     * 
     * @param token The JWT token to validate
     * @param username The expected username (email)
     * @return boolean True if valid, false otherwise
     */
    public boolean validateToken(String token, String username) {
        try {
            logger.debug("Validating JWT token for user: {}", username);
            final String extractedUsername = extractUsername(token);
            boolean isValid = (extractedUsername.equals(username) && !isTokenExpired(token));
            if (isValid) {
                logger.debug("JWT token validation successful for user: {}", username);
            } else {
                logger.warn("JWT token validation failed for user: {}", username);
            }
            return isValid;
        } catch (Exception e) {
            logger.error("JWT token validation error for user {}: {}", username, e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if token has expired
     * 
     * @param token The JWT token
     * @return boolean True if expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Extract the role from a token
     * 
     * Used to get user's role without querying the database
     * 
     * @param token The JWT token
     * @return String The user's role
     */
    public String extractRole(String token) {
        try {
            logger.debug("Extracting role from JWT token");
            Claims claims = extractAllClaims(token);
            String role = claims.get("role", String.class);
            logger.debug("Successfully extracted role: {}", role);
            return role;
        } catch (Exception e) {
            logger.error("Failed to extract role from token: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * PRIVATE HELPER - Extract all claims from token
     * 
     * Claims are the data embedded in the token (subject, expiry, etc.)
     * We parse the token using our secret key to verify it hasn't been tampered with.
     * 
     * @param token The JWT token
     * @return Claims object containing all the token data
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // Verify signature using our secret key
                .build()
                .parseSignedClaims(token)     // Parse the token
                .getPayload();                // Extract the claims (payload)
    }
    
    /**
     * PRIVATE HELPER - Get the signing key
     * 
     * Converts our secret string into a SecretKey object that JJWT can use.
     * The key must be converted to bytes using UTF-8 encoding.
     * 
     * @return SecretKey The signing key for HMAC-SHA256
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
