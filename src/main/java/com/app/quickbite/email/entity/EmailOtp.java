package com.app.quickbite.email.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * EMAIL OTP ENTITY - Stores One-Time Password for email verification
 * 
 * This entity tracks:
 * - OTP codes sent to users
 * - Expiration times (5 minutes)
 * - Verification attempts and status
 * - Rate limiting data
 */
@Entity
@Table(name = "email_otps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailOtp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;
    
    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;
    
    
    public EmailOtp(String email, String otpCode, LocalDateTime date) {
    	this.email=email;
    	this.otpCode=otpCode;
    	this.createdAt = LocalDateTime.now();
    	this.expiresAt=date;
    }


	public boolean isMaxAttemptsReached() {
		// TODO Auto-generated method stub
		return this.attempts>=3;
	}
}