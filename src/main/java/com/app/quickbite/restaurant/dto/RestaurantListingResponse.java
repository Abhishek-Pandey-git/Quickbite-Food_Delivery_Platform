package com.app.quickbite.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for public restaurant listing (for customers browsing)
 * 
 * Contains only information customers need to see.
 * Does NOT include owner details or internal flags.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantListingResponse {

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
     * Number of menu items (for display purposes)
     */
    private Integer menuItemCount;
}
