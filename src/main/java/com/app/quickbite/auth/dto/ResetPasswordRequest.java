package com.app.quickbite.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for resetting password after OTP verification
 */
@Schema(description = "Request payload for resetting password with OTP")
public class ResetPasswordRequest {
    
    @Schema(description = "Email address of the user", example = "user@example.com", required = true)
    private String email;
    
    @Schema(description = "OTP code received via email", example = "123456", required = true)
    private String otpCode;
    
    @Schema(description = "New password", example = "newPassword123", required = true)
    private String newPassword;
    
    public ResetPasswordRequest() {}
    
    public ResetPasswordRequest(String email, String otpCode, String newPassword) {
        this.email = email;
        this.otpCode = otpCode;
        this.newPassword = newPassword;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getOtpCode() {
        return otpCode;
    }
    
    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
