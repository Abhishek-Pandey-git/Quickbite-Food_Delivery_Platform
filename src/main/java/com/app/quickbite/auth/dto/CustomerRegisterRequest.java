package com.app.quickbite.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CUSTOMER REGISTRATION REQUEST DTO - Data sent when a customer registers
 * 
 * Example JSON request body:
 * {
 *   "fullName": "John Doe",
 *   "email": "john@example.com",
 *   "password": "myPassword123",
 *   "phone": "+1-555-0123"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisterRequest {
    
    /**
     * Customer's full name
     */
    private String fullName;
    
    /**
     * Email address (must be unique, will be used as username)
     */
    private String email;
    
    /**
     * Plain text password (will be hashed before storing)
     */
    private String password;
    
    /**
     * Phone number for contact
     */
    private String phone;
}
