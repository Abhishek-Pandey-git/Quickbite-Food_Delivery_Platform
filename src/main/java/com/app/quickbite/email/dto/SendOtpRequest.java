package com.app.quickbite.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Request DTO for sending OTP to email
 */

@Data
@AllArgsConstructor
public class SendOtpRequest {
    
    private String email;
    
    public SendOtpRequest() {}
    
}