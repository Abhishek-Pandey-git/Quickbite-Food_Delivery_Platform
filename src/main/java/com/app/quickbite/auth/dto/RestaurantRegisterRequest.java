package com.app.quickbite.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESTAURANT REGISTRATION REQUEST DTO - Data sent when a restaurant owner registers
 * 
 * This includes all customer fields PLUS restaurant-specific information.
 * 
 * Example JSON request body:
 * {
 *   "fullName": "Jane Smith",
 *   "email": "jane@pizzapalace.com",
 *   "password": "securePassword456",
 *   "phone": "+1-555-0456",
 *   "restaurantName": "Pizza Palace",
 *   "address": "123 Main St, New York, NY 10001",
 *   "cuisineType": "Italian"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRegisterRequest {
    
    /**
     * Owner's full name
     */
    private String fullName;
    
    /**
     * Owner's email (will be used as username)
     */
    private String email;
    
    /**
     * Owner's password (will be hashed)
     */
    private String password;
    
    /**
     * Owner's phone number
     */
    private String phone;
    
    /**
     * Name of the restaurant
     */
    private String restaurantName;
    
    /**
     * Restaurant's physical address
     */
    private String address;
    
    /**
     * Type of cuisine (e.g., "Italian", "Chinese", "Fast Food")
     */
    private String cuisineType;
}
