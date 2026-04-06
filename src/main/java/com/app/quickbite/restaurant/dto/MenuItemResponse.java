package com.app.quickbite.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for returning menu item information
 * 
 * Used as the response when fetching menu items.
 * Contains all public-facing menu item details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {

    /**
     * Menu item's unique identifier
     */
    private Long id;

    /**
     * Name of the menu item
     */
    private String name;

    /**
     * Description of the dish
     */
    private String description;

    /**
     * Price of the menu item
     */
    private BigDecimal price;

    /**
     * Category of the dish
     */
    private String category;

    /**
     * Whether the item is currently available
     */
    private Boolean isAvailable;

    /**
     * ID of the restaurant this item belongs to
     */
    private Long restaurantId;
}
