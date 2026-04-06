package com.app.quickbite.restaurant.repository;

import com.app.quickbite.restaurant.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MENU ITEM REPOSITORY - Data Access Layer for MenuItem entity
 * 
 * Provides CRUD operations for MenuItem records.
 * Includes custom query methods for restaurant-specific menu retrieval.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Find all menu items for a specific restaurant
     * 
     * @param restaurantId The ID of the restaurant
     * @return List of all menu items belonging to the restaurant
     */
    List<MenuItem> findByRestaurantId(Long restaurantId);

    /**
     * Find all available menu items for a specific restaurant
     * Useful for customer-facing views where unavailable items should be hidden
     * 
     * @param restaurantId The ID of the restaurant
     * @param isAvailable Filter by availability status
     * @return List of menu items matching the criteria
     */
    List<MenuItem> findByRestaurantIdAndIsAvailable(Long restaurantId, Boolean isAvailable);

    /**
     * Find a specific menu item by ID and restaurant ID
     * Used to verify that a menu item belongs to a restaurant before updates/deletes
     * 
     * @param id The menu item ID
     * @param restaurantId The restaurant ID
     * @return Optional containing the menu item if found and belongs to the restaurant
     */
    Optional<MenuItem> findByIdAndRestaurantId(Long id, Long restaurantId);

    /**
     * Find all menu items by category for a specific restaurant
     * Useful for category-based menu display
     * 
     * @param restaurantId The ID of the restaurant
     * @param category The category to filter by
     * @return List of menu items in the specified category
     */
    List<MenuItem> findByRestaurantIdAndCategory(Long restaurantId, String category);
}
