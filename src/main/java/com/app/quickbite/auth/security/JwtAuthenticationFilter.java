package com.app.quickbite.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT AUTHENTICATION FILTER - Intercepts Every Request
 * 
 * This filter runs ONCE for EVERY incoming HTTP request (before it reaches the controller).
 * 
 * THE FLOW:
 * 1. User sends a request with a JWT token in the Authorization header
 *    Example: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1...
 * 
 * 2. This filter intercepts the request and:
 *    a. Extracts the token from the Authorization header
 *    b. Validates the token using JwtUtil
 *    c. Loads the user details from the database
 *    d. Sets the authentication in Spring Security's SecurityContext
 * 
 * 3. If authentication is successful, Spring Security allows the request to proceed
 *    If not, the request is rejected with 401 Unauthorized
 * 
 * WHY OncePerRequestFilter?
 * - Regular filters might execute multiple times for a single request (e.g., forwarding, including)
 * - OncePerRequestFilter guarantees it runs exactly once per request
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
 
    private JwtUtil jwtUtil; // To validate and extract info from JWT tokens
    
  
    private UserDetailsService userDetailsService; // To load user from database

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        logger.info("JwtAuthenticationFilter initialized");
    }
    
    /**
     * This method is called for EVERY request
     * 
     * @param request The incoming HTTP request
     * @param response The HTTP response (we can modify it if needed)
     * @param filterChain The chain of filters - we must call this to continue processing the request
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        
        // STEP 1: Extract the Authorization header from the request
        // Example header: "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
        final String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;
        
        // STEP 2: Check if the header exists and starts with "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            logger.debug("Authorization header found, extracting JWT token");
            // Extract the token (remove "Bearer " prefix)
            jwt = authorizationHeader.substring(7); // "Bearer " is 7 characters long
            
            try {
                // Extract the username (email) from the token
                username = jwtUtil.extractUsername(jwt);
                logger.debug("JWT token extracted successfully for user: {}", username);
            } catch (Exception e) {
                // If token is malformed or invalid, extractUsername will throw an exception
                // We catch it here and continue without setting authentication
                // (request will be rejected as unauthenticated)
                logger.warn("JWT Token extraction failed: {}", e.getMessage());
            }
        }
        
        // STEP 3: If we successfully extracted a username AND there's no existing authentication
        // (SecurityContext is empty), then we need to authenticate this request
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Authenticating user: {}", username);
            
            // Load user details from database using the email
            // UserDetailsService is provided by Spring Security - we'll configure it in SecurityConfig
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // STEP 4: Validate the token
            // Check if the token is valid (not expired, matches the username, signature is correct)
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                logger.info("JWT token validation successful, setting authentication for user: {}", username);
                
                // STEP 5: Create an authentication token
                // This tells Spring Security: "This user is authenticated!"
                // We use UsernamePasswordAuthenticationToken (even though we're using JWT)
                // because it's a standard Spring Security class for authenticated users
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,           // Principal: who is the user?
                        null,                  // Credentials: we don't need password here (already verified)
                        userDetails.getAuthorities() // Authorities: user's roles/permissions
                    );
                
                // Attach additional details about the request (IP address, session ID, etc.)
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // STEP 6: Set the authentication in the SecurityContext
                // This is THE KEY STEP - it tells Spring Security that this user is authenticated
                // From this point on, the user can access protected endpoints based on their role
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                logger.warn("JWT token validation failed for user: {}", username);
            }
        } else {
            if (username == null && authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                logger.debug("Failed to extract username from JWT token");
            } else if (username != null) {
                logger.debug("User already authenticated in SecurityContext: {}", username);
            } else {
                logger.debug("No Authorization header or doesn't start with 'Bearer '");
            }
        }
        
        // STEP 7: Continue the filter chain
        // This passes the request to the next filter (or the controller if this is the last filter)
        // Even if authentication failed, we continue - Spring Security will handle authorization
        logger.debug("Continuing filter chain for request: {} {}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
