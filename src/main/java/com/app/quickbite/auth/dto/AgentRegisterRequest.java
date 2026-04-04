package com.app.quickbite.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AGENT REGISTRATION REQUEST DTO - Data sent when a delivery agent registers
 * 
 * This includes all customer fields PLUS delivery agent-specific information.
 * 
 * Example JSON request body:
 * {
 *   "fullName": "Mike Johnson",
 *   "email": "mike@delivery.com",
 *   "password": "deliveryPass789",
 *   "phone": "+1-555-0789",
 *   "vehicleNumber": "KA-01-AB-1234"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentRegisterRequest {
    
    /**
     * Agent's full name
     */
    private String fullName;
    
    /**
     * Agent's email (will be used as username)
     */
    private String email;
    
    /**
     * Agent's password (will be hashed)
     */
    private String password;
    
    /**
     * Agent's phone number
     */
    private String phone;
    
    /**
     * Vehicle registration number (e.g., "AB12 CD34" or "KA-01-AB-1234")
     */
    private String vehicleNumber;

    /**
     * Address which will store city in india
      
     */
    private String address;
}
