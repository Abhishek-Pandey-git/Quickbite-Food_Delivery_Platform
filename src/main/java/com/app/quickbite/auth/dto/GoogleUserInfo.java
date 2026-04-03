package com.app.quickbite.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * User information from Google's userinfo endpoint
 * Contains user's profile data from Google
 */
@Data
public class GoogleUserInfo {
    
    /**
     * Google's unique identifier for the user (sub = subject)
     */
    @JsonProperty("sub")
    private String sub;
    
    /**
     * User's email address
     */
    @JsonProperty("email")
    private String email;
    
    /**
     * Whether Google has verified this email
     */
    @JsonProperty("email_verified")
    private Boolean emailVerified;
    
    /**
     * User's full name
     */
    @JsonProperty("name")
    private String name;
}