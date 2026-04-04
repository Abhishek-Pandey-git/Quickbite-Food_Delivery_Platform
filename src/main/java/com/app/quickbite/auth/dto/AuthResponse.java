package com.app.quickbite.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AUTH RESPONSE DTO - Data sent back after successful registration or login
 * 
 * This is returned to the client after a successful authentication operation.
 * The client should store the token and send it with subsequent requests.
 * 
 * Example JSON response body:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIn0...",
 *   "role": "CUSTOMER",
 *   "message": "Login successful"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    /**
     * JWT token - the client should include this in the Authorization header
     * for all future requests: "Authorization: Bearer <token>"
     */
    private String token;
    
    /**
     * User's role (CUSTOMER, RESTAURANT_OWNER, DELIVERY_AGENT, ADMIN)
     * The frontend can use this to show different UI based on role
     */
    private String role;
    
    /**
     * Success message to display to the user
     */
    private String message;
}
