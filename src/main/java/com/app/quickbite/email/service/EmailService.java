package com.app.quickbite.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * EMAIL SERVICE - Send emails via SMTP
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${quickbite.email.from-address}")
    private String fromAddress;
    
    @Value("${quickbite.email.from-name}")
    private String fromName;
    
    /**
     * Send OTP verification email (async)
     */
    @Async
    public void sendOtpEmail(String toEmail, String otpCode, String userName) {
        logger.info("Sending OTP email to: {}", toEmail);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("QuickBite - Email Verification Code");
            message.setText(buildOtpEmailContent(userName, otpCode));
            
            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    
    /**
     * Build email content for OTP
     */
    private String buildOtpEmailContent(String userName, String otpCode) {
        return String.format("""
            Hi %s,
            
            Welcome to QuickBite! 🍕
            
            Your email verification code is:
            
            %s
            
            This code will expire in 5 minutes.
            
            If you didn't request this verification, please ignore this email.
            
            Best regards,
            The QuickBite Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, userName, otpCode);
    }
}