package com.app.quickbite.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for forgot password request - sends OTP to email
 */
@Schema(description = "Request payload for initiating password reset")
public class ForgotPasswordRequest {
    
    @Schema(description = "Email address of the user", example = "user@example.com", required = true)
    private String email;
    
    public ForgotPasswordRequest() {}
    
    public ForgotPasswordRequest(String email) {
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
