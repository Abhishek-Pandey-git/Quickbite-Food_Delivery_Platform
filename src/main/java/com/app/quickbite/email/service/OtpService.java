package com.app.quickbite.email.service;

import com.app.quickbite.email.entity.EmailOtp;
import com.app.quickbite.email.repository.EmailOtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * OTP SERVICE - One-Time Password generation and validation
 */
@Service
public class OtpService {
    
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom random = new SecureRandom();
    
    @Autowired
    private EmailOtpRepository otpRepository;
    
    @Value("${quickbite.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;
    
    @Value("${quickbite.otp.code-length:6}")
    private int otpCodeLength;
    
    @Value("${quickbite.otp.max-attempts:3}")
    private int maxAttempts;
    
    /**
     * Generate a new OTP for email verification
     */
    public String generateOtp(String email) {
        logger.info("Generating OTP for email: {}", email);
        
        // Check rate limiting (max 3 OTPs per hour)
        if (isRateLimited(email)) {
            logger.warn("Rate limit exceeded for email: {}", email);
            throw new RuntimeException("Too many OTP requests. Please try again later.");
        }
        
        // Generate random 6-digit code
        String otpCode = generateRandomCode();
        
        // Set expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);
        
        // Save OTP to database
        EmailOtp otp = new EmailOtp(email, otpCode, expiresAt);
        otpRepository.save(otp);
        
        logger.info("OTP generated and saved for email: {}", email);
        return otpCode;
    }
    
    /**
     * Verify OTP code
     */
    public boolean verifyOtp(String email, String otpCode) {
        logger.info("Verifying OTP for email: {}", email);
        
        Optional<EmailOtp> otpOpt = otpRepository.findValidOtp(email, otpCode, LocalDateTime.now());
        
        if (otpOpt.isEmpty()) {
            logger.warn("Invalid or expired OTP for email: {}", email);
            
            // Increment attempts for any existing active OTP
            incrementAttempts(email);
            return false;
        }
        
        EmailOtp otp = otpOpt.get();
        
        // Mark as verified
        otp.setVerified(true);
        otpRepository.save(otp);
        
        logger.info("OTP verified successfully for email: {}", email);
        return true;
    }
    
    /**
     * Generate random numeric code
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < otpCodeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * Check if email is rate limited (more than 3 OTPs in last hour)
     */
    private boolean isRateLimited(String email) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Long count = otpRepository.countOtpsSentInLastHour(email, oneHourAgo);
        return count >= 3;
    }
    
    /**
     * Increment attempts for active OTP
     */
    private void incrementAttempts(String email) {
        Optional<EmailOtp> activeOtpOpt = otpRepository.findLatestActiveOtpByEmail(email);
        if (activeOtpOpt.isPresent()) {
            EmailOtp activeOtp = activeOtpOpt.get();
            activeOtp.setAttempts(activeOtp.getAttempts() + 1);
            otpRepository.save(activeOtp);
            
            if (activeOtp.isMaxAttemptsReached()) {
                logger.warn("Maximum attempts reached for email: {}", email);
            }
        }
    }
}