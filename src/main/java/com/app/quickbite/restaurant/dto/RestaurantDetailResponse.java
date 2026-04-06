package com.app.quickbite.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for detailed restaurant view (when customer clicks on a restaurant)
 * 
 * Includes restaurant info plus the menu items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDetailResponse {

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
     * List of available menu items
     */
    private List<MenuItemResponse> menuItems;
}
