package com.app.quickbite.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LOGIN REQUEST DTO - Data sent when user logs in
 * 
 * DTO (Data Transfer Object) is a simple object that carries data between client and server.
 * It doesn't have business logic, just fields with getters/setters.
 * 
 * Example JSON request body:
 * {
 *   "email": "john@example.com",
 *   "password": "myPassword123"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    /**
     * User's email address (used as username)
     */
    private String email;
    
    /**
     * User's plain text password (will be compared with hashed password in database)
     */
    private String password;
}
