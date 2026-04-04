package com.app.quickbite.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning restaurant profile information
 * 
 * Used as the response when fetching restaurant details.
 * Contains all public-facing restaurant information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantProfileResponse {

    /**
     * Restaurant's unique identifier
     */
    private Long id;

    /**
     * Name of the restaurant
     */
    private String restaurantName;

    /**
     * Physical address of the restaurant
     */
    private String address;

    /**
     * Type of cuisine served
     */
    private String cuisineType;

    /**
     * Whether the restaurant is currently open for orders
     */
    private Boolean isOpen;

    /**
     * Whether the restaurant has been approved by admin
     */
    private Boolean isApproved;

    /**
     * Owner's full name (from linked User entity)
     */
    private String ownerName;

    /**
     * Owner's email (from linked User entity)
     */
    private String ownerEmail;

    /**
     * Owner's phone number (from linked User entity)
     */
    private String ownerPhone;
}
