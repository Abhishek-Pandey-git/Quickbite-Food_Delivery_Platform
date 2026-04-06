package com.app.quickbite.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for OAuth2 authentication
 * Sent to frontend after successful OAuth2 login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2AuthResponse {
    
    /**
     * JWT token for QuickBite authentication
     */
    private String token;
    
    /**
     * User's role (always CUSTOMER for OAuth2)
     */
    private String role;
    
    /**
     * User's email address
     */
    private String email;
    
    /**
     * Success message
     */
    private String message;
}