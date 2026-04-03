package com.app.quickbite.email.controller;

import com.app.quickbite.auth.repository.UserRepository;
import com.app.quickbite.email.dto.OtpResponse;
import com.app.quickbite.email.dto.SendOtpRequest;
import com.app.quickbite.email.dto.VerifyOtpRequest;
import com.app.quickbite.email.service.EmailService;
import com.app.quickbite.email.service.OtpService;
import com.app.quickbite.auth.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * OTP CONTROLLER - Email verification endpoints
 */
@RestController
@RequestMapping("/api/otp")
@Tag(name = "OTP Verification", description = "Email verification using One-Time Password (OTP)")
public class OtpController {
    
    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);
    
    @Autowired
    private OtpService otpService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Send OTP to email for verification
     */
    @Operation(
        summary = "Send OTP to email",
        description = "Generate and send a 6-digit OTP code to the user's email for verification. OTP expires in 5 minutes.",
        tags = {"OTP Verification"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "OTP sent successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OtpResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "OTP sent to your email. Please check your inbox."
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit exceeded",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/send")
    public ResponseEntity<OtpResponse> sendOtp(
            @Parameter(description = "Email to send OTP to", required = true)
            @RequestBody SendOtpRequest request) {
        
        logger.info("OTP send request for email: {}", request.getEmail());
        
        try {
            // Check if user exists
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                logger.warn("OTP requested for non-existent email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new OtpResponse(false, "User not found"));
            }
            
            User user = userOpt.get();
            
            // Check if already verified
            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                logger.info("Email already verified: {}", request.getEmail());
                return ResponseEntity.ok(new OtpResponse(false, "Email already verified"));
            }
            
            // Generate OTP
            String otpCode = otpService.generateOtp(request.getEmail());
            
            // Send email
            emailService.sendOtpEmail(request.getEmail(), otpCode, user.getFullName());
            
            logger.info("OTP sent successfully to: {}", request.getEmail());
            return ResponseEntity.ok(new OtpResponse(true, "OTP sent to your email. Please check your inbox."));
            
        } catch (RuntimeException e) {
            logger.error("Error sending OTP to {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new OtpResponse(false, e.getMessage()));
        }
    }
    
    /**
     * Verify OTP code
     */
    @Operation(
        summary = "Verify OTP code",
        description = "Verify the OTP code sent to the user's email. If valid, marks the email as verified.",
        tags = {"OTP Verification"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "OTP verification result",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OtpResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "Email verified successfully"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired OTP",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/verify")
    public ResponseEntity<OtpResponse> verifyOtp(
            @Parameter(description = "OTP verification details", required = true)
            @RequestBody VerifyOtpRequest request) {
        
        logger.info("OTP verification request for email: {}", request.getEmail());
        
        try {
            // Find user
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                logger.warn("OTP verification failed - user not found: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new OtpResponse(false, "User not found"));
            }
            
            User user = userOpt.get();
            
            // Verify OTP
            boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtpCode());
            
            if (isValid) {
                // Mark email as verified
                user.setEmailVerified(true);
                userRepository.save(user);
                
                logger.info("Email verified successfully: {}", request.getEmail());
                return ResponseEntity.ok(new OtpResponse(true, "Email verified successfully"));
            } else {
                logger.warn("Invalid or expired OTP for email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new OtpResponse(false, "Invalid or expired OTP"));
            }
            
        } catch (Exception e) {
            logger.error("Error verifying OTP for {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new OtpResponse(false, "Error verifying OTP"));
        }
    }
}