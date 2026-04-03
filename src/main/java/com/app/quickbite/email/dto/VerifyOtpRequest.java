package com.app.quickbite.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Request DTO for verifying OTP
 */
@Data
@AllArgsConstructor
public class VerifyOtpRequest {
    
    private String email;
    private String otpCode;
    
    public VerifyOtpRequest() {}
    
    
}