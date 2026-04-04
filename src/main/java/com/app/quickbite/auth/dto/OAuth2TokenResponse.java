package com.app.quickbite.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Response DTO from Google's token endpoint
 * Google returns this when we exchange auth code
 */
@Data
public class OAuth2TokenResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("expires_in")
    private Integer expiresIn;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("scope")
    private String scope;
    
    @JsonProperty("id_token")
    private String idToken;
}