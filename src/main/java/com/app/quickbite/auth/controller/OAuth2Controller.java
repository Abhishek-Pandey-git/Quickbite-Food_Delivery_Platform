package com.app.quickbite.auth.controller;

import com.app.quickbite.auth.dto.OAuth2AuthResponse;
import com.app.quickbite.auth.dto.OAuth2TokenRequest;
import com.app.quickbite.auth.service.OAuth2UserService;
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

/**
 * OAuth2 Controller
 * Handles Google OAuth2 authentication for customer users
 */
@RestController
@RequestMapping("/api/auth/oauth2")
@Tag(name = "OAuth2 Authentication", description = "Google OAuth2 login endpoints for customers")
public class OAuth2Controller {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);

    @Autowired
    private OAuth2UserService oauth2UserService;

    /**
     * OAuth2 Token Exchange Endpoint
     * 
     * Frontend flow:
     * 1. User clicks "Login with Google"
     * 2. Frontend redirects to Google consent screen
     * 3. User approves, Google redirects back to frontend with auth code
     * 4. Frontend sends the code to this endpoint
     * 5. Backend exchanges code for access token, fetches user info, creates/updates user
     * 6. Backend returns QuickBite JWT token
     * 
     * Endpoint: POST /api/auth/oauth2/token
     * 
     * Request Body (JSON):
     * {
     *   "code": "4/0AX4XfWh...",
     *   "redirectUri": "http://localhost:3000/auth/callback"
     * }
     * 
     * Response (JSON) - Success (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "role": "CUSTOMER",
     *   "email": "user@gmail.com",
     *   "message": "Google login successful"
     * }
     * 
     * @param request OAuth2TokenRequest containing auth code and redirect URI
     * @return ResponseEntity<OAuth2AuthResponse> JWT token and user info
     */
    @Operation(
        summary = "Google OAuth2 token exchange",
        description = """
            Exchange Google authorization code for QuickBite JWT token.
            
            This endpoint is called by the frontend after user approves Google login.
            It exchanges the authorization code for user information and issues a QuickBite JWT token.
            
            **Only for Customer users** - OAuth2 login is not available for Restaurant Owners or Delivery Agents.
            
            **Email Auto-Verified** - Users logging in via Google have their email automatically verified.
            """,
        tags = {"OAuth2 Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "OAuth2 login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OAuth2AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "role": "CUSTOMER",
                      "email": "user@gmail.com",
                      "message": "Google login successful"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid authorization code or redirect URI",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email already registered with password authentication",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Failed to exchange token or fetch user info from Google",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/token")
    public ResponseEntity<OAuth2AuthResponse> exchangeToken(
            @Parameter(description = "OAuth2 token request containing authorization code", required = true)
            @RequestBody OAuth2TokenRequest request) {
        
        logger.info("OAuth2 token exchange request received");
        logger.debug("Authorization code received (length: {})", request.getCode().length());
        
        try {
            OAuth2AuthResponse response = oauth2UserService.authenticateWithGoogle(request);
            logger.info("OAuth2 authentication successful for email: {}", response.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("OAuth2 authentication failed: {}", e.getMessage());
            throw e; // Will be handled by GlobalExceptionHandler
        }
    }
}