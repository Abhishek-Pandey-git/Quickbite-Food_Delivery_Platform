package com.app.quickbite.auth.controller;

import com.app.quickbite.auth.dto.*;
import com.app.quickbite.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * AUTH CONTROLLER - REST API Endpoints for Authentication
 * 
 * This controller exposes HTTP endpoints for user registration and login.
 * All endpoints are public (no authentication required) as defined in SecurityConfig.
 * 
 * @RestController: Marks this as a REST controller (returns JSON, not HTML)
 * @RequestMapping: All endpoints in this controller start with "/api/auth"
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints for the QuickBite platform")
public class AuthController {

    private static final Logger logger=LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * CUSTOMER REGISTRATION ENDPOINT
     * 
     * Endpoint: POST /api/auth/register/customer
     * 
     * Request Body (JSON):
     * {
     *   "fullName": "John Doe",
     *   "email": "john@example.com",
     *   "password": "myPassword123",
     *   "phone": "+1-555-0123"
     * }
     * 
     * Response (JSON) - Success (201 CREATED):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "role": "CUSTOMER",
     *   "message": "Customer registration successful"
     * }
     * 
     * Response (JSON) - Email already exists (409 CONFLICT):
     * {
     *   "timestamp": "2026-04-02T13:37:00",
     *   "status": 409,
     *   "error": "User Already Exists",
     *   "message": "User with email john@example.com already exists"
     * }
     * 
     * @param request CustomerRegisterRequest DTO
     * @return ResponseEntity<AuthResponse> HTTP 201 with token on success
     */
    @Operation(
        summary = "Register a new customer",
        description = """
            Register a new customer account in the QuickBite platform.
            
            Creates a User entity with role CUSTOMER and generates a JWT token for immediate authentication.
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Customer registration successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "role": "CUSTOMER",
                      "message": "Customer registration successful"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "User with this email already exists",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/register/customer")
    public ResponseEntity<AuthResponse> registerCustomer(
            @Parameter(description = "Customer registration details", required = true)
            @RequestBody CustomerRegisterRequest request) {
        logger.info("Customer registration request received for mail: {}", request.getEmail());
        AuthResponse response = authService.registerCustomer(request);
        logger.info("Customer registration successful for mail: {}", request.getEmail());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * RESTAURANT REGISTRATION ENDPOINT
     * 
     * Endpoint: POST /api/auth/register/restaurant
     * 
     * Request Body (JSON):
     * {
     *   "fullName": "Jane Smith",
     *   "email": "jane@pizzapalace.com",
     *   "password": "securePassword456",
     *   "phone": "+1-555-0456",
     *   "restaurantName": "Pizza Palace",
     *   "address": "123 Main St, New York, NY 10001",
     *   "cuisineType": "Italian"
     * }
     * 
     * Response (JSON) - Success (201 CREATED):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "role": "RESTAURANT_OWNER",
     *   "message": "Restaurant registration successful. Pending admin approval."
     * }
     * 
     * @param request RestaurantRegisterRequest DTO
     * @return ResponseEntity<AuthResponse> HTTP 201 with token on success
     */
    @Operation(
        summary = "Register a new restaurant owner",
        description = """
            Register a new restaurant owner account in the QuickBite platform.
            
            Creates a User entity with role RESTAURANT_OWNER and a linked Restaurant entity.
            Restaurant is pending approval by default.
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Restaurant registration successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "role": "RESTAURANT_OWNER", 
                      "message": "Restaurant registration successful. Pending admin approval."
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "User with this email already exists",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/register/restaurant")
    public ResponseEntity<AuthResponse> registerRestaurant(
            @Parameter(description = "Restaurant owner registration details", required = true)
            @RequestBody RestaurantRegisterRequest request) {
        AuthResponse response = authService.registerRestaurant(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * DELIVERY AGENT REGISTRATION ENDPOINT
     * 
     * Endpoint: POST /api/auth/register/agent
     * 
     * Request Body (JSON):
     * {
     *   "fullName": "Mike Johnson",
     *   "email": "mike@delivery.com",
     *   "password": "deliveryPass789",
     *   "phone": "+1-555-0789",
     *   "vehicleNumber": "KA-01-AB-1234"
     * }
     * 
     * Response (JSON) - Success (201 CREATED):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "role": "DELIVERY_AGENT",
     *   "message": "Delivery agent registration successful. Pending verification."
     * }
     * 
     * @param request AgentRegisterRequest DTO
     * @return ResponseEntity<AuthResponse> HTTP 201 with token on success
     */
    @Operation(
        summary = "Register a new delivery agent",
        description = """
            Register a new delivery agent account in the QuickBite platform.
            
            Creates a User entity with role DELIVERY_AGENT and a linked DeliveryAgent entity.
            Agent is pending verification by default.
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Delivery agent registration successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "role": "DELIVERY_AGENT",
                      "message": "Delivery agent registration successful. Pending verification."
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "User with this email already exists",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/register/agent")
    public ResponseEntity<AuthResponse> registerAgent(
            @Parameter(description = "Delivery agent registration details", required = true)
            @RequestBody AgentRegisterRequest request) {
        AuthResponse response = authService.registerAgent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * LOGIN ENDPOINT
     * 
     * Endpoint: POST /api/auth/login
     * 
     * Request Body (JSON):
     * {
     *   "email": "john@example.com",
     *   "password": "myPassword123"
     * }
     * 
     * Response (JSON) - Success (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "role": "CUSTOMER",
     *   "message": "Login successful"
     * }
     * 
     * Response (JSON) - Invalid credentials (401 UNAUTHORIZED):
     * {
     *   "timestamp": "2026-04-02T13:37:00",
     *   "status": 401,
     *   "error": "Authentication Failed",
     *   "message": "Invalid email or password"
     * }
     * 
     * THE FLOW:
     * 1. Client sends email + password
     * 2. AuthService uses Spring Security's AuthenticationManager to verify credentials
     * 3. If valid, generate JWT token and return it
     * 4. Client stores the token and includes it in future requests:
     *    Header: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * 
     * @param request LoginRequest DTO
     * @return ResponseEntity<AuthResponse> HTTP 200 with token on success
     */
    @Operation(
        summary = "User login",
        description = """
            Authenticate a user and receive a JWT token for accessing protected endpoints.
            
            Supports login for all user types (Customer, Restaurant Owner, Delivery Agent).
            The returned role indicates the user's permissions in the system.
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "role": "CUSTOMER",
                      "message": "Login successful"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid email or password",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request format",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Login credentials", required = true)
            @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response); // HTTP 200 OK
    }
    
    /**
     * FORGOT PASSWORD ENDPOINT
     * 
     * Endpoint: POST /api/auth/forgot-password
     * 
     * Request Body (JSON):
     * {
     *   "email": "user@example.com"
     * }
     * 
     * Response (JSON) - Success (200 OK):
     * {
     *   "message": "Password reset code sent to your email",
     *   "email": "user@example.com"
     * }
     */
    @Operation(
        summary = "Request password reset",
        description = """
            Initiate password reset by sending OTP to user's email.
            The OTP will be valid for 5 minutes.
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset OTP sent successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No account found with this email",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Parameter(description = "Email address for password reset", required = true)
            @RequestBody ForgotPasswordRequest request) {
        logger.info("Forgot password request received for email: {}", request.getEmail());
        var response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * RESET PASSWORD ENDPOINT
     * 
     * Endpoint: POST /api/auth/reset-password
     * 
     * Request Body (JSON):
     * {
     *   "email": "user@example.com",
     *   "otpCode": "123456",
     *   "newPassword": "newSecurePassword123"
     * }
     * 
     * Response (JSON) - Success (200 OK):
     * {
     *   "message": "Password reset successful. You can now login with your new password."
     * }
     */
    @Operation(
        summary = "Reset password with OTP",
        description = """
            Reset user password after verifying the OTP sent to their email.
            Requires valid OTP and new password.
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset successful",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired OTP",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Parameter(description = "OTP verification and new password", required = true)
            @RequestBody ResetPasswordRequest request) {
        logger.info("Password reset request received for email: {}", request.getEmail());
        var response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}
