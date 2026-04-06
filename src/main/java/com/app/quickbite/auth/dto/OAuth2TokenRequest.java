package com.app.quickbite.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for OAuth2 token exchange
 * Frontend sends this after user approves Google login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2TokenRequest {
    
    /**
     * Authorization code from Google
     * Frontend gets this from Google's redirect
     */
    private String code;
    
    /**
     * Redirect URI that was used (must match Google config)
     */
    private String redirectUri;
}