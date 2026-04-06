package com.app.quickbite.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a menu item
 * 
 * Used when a restaurant owner adds a new item or updates existing one.
 * Includes validation annotations for input validation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequest {

    /**
     * Name of the menu item
     */
    @NotBlank(message = "Item name is required")
    private String name;

    /**
     * Description of the dish
     */
    private String description;

    /**
     * Price of the menu item
     * Must be a positive value
     */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    /**
     * Category of the dish (e.g., "Appetizers", "Main Course", "Desserts")
     */
    @NotBlank(message = "Category is required")
    private String category;

    /**
     * Availability status
     * Defaults to true if not provided
     */
    private Boolean isAvailable = true;
}
