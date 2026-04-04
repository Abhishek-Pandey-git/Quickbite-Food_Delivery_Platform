package com.app.quickbite.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating restaurant profile information
 * 
 * Used when a restaurant owner wants to update their restaurant details.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantProfileUpdateRequest {

    /**
     * Name of the restaurant
     */
    private String restaurantName;

    /**
     * Physical address of the restaurant
     */
    private String address;

    /**
     * Type of cuisine served (e.g., "Italian", "Chinese", "Fast Food")
     */
    private String cuisineType;

    /**
     * Whether the restaurant is currently open for orders
     */
    private Boolean isOpen;
}
