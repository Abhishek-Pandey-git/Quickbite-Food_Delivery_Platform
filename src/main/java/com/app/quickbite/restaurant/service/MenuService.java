package com.app.quickbite.restaurant.service;

import com.app.quickbite.auth.entity.Restaurant;
import com.app.quickbite.auth.repository.RestaurantRepository;
import com.app.quickbite.restaurant.dto.MenuItemRequest;
import com.app.quickbite.restaurant.dto.MenuItemResponse;
import com.app.quickbite.restaurant.entity.MenuItem;
import com.app.quickbite.restaurant.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MENU SERVICE - Business Logic for Menu Item Management
 * 
 * Handles all CRUD operations for menu items:
 * - Adding new menu items
 * - Getting menu items (all or by category)
 * - Updating menu items
 * - Deleting menu items
 * - Toggling availability
 */
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Add a new menu item to the restaurant
     * 
     * @param ownerEmail Email of the restaurant owner (from JWT)
     * @param request DTO containing menu item details
     * @return MenuItemResponse with the created item details
     * @throws IllegalArgumentException if restaurant not found
     */
    @Transactional
    public MenuItemResponse addMenuItem(String ownerEmail, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found for email: " + ownerEmail));

        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurant(restaurant);
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

        MenuItem savedItem = menuItemRepository.save(menuItem);
        return mapToResponse(savedItem);
    }

    /**
     * Get all menu items for the authenticated restaurant owner
     * 
     * @param ownerEmail Email of the restaurant owner (from JWT)
     * @return List of all menu items for the restaurant
     */
    public List<MenuItemResponse> getMenuItems(String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found for email: " + ownerEmail));

        return menuItemRepository.findByRestaurantId(restaurant.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all menu items for a restaurant (public endpoint)
     * Only returns available items for public view
     * 
     * @param restaurantId ID of the restaurant
     * @return List of available menu items
     */
    public List<MenuItemResponse> getMenuItemsByRestaurantId(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsAvailable(restaurantId, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get menu items by category
     * 
     * @param ownerEmail Email of the restaurant owner (from JWT)
     * @param category The category to filter by
     * @return List of menu items in the specified category
     */
    public List<MenuItemResponse> getMenuItemsByCategory(String ownerEmail, String category) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found for email: " + ownerEmail));

        return menuItemRepository.findByRestaurantIdAndCategory(restaurant.getId(), category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing menu item
     * 
     * @param ownerEmail Email of the restaurant owner (from JWT)
     * @param itemId ID of the menu item to update
     * @param request DTO containing updated details
     * @return Updated MenuItemResponse
     * @throws IllegalArgumentException if restaurant or menu item not found
     */
    @Transactional
    public MenuItemResponse updateMenuItem(String ownerEmail, Long itemId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found for email: " + ownerEmail));

        // Verify the menu item belongs to this restaurant
        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(itemId, restaurant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found or doesn't belong to your restaurant"));

        // Update fields
        if (request.getName() != null) {
            menuItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            menuItem.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            menuItem.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            menuItem.setCategory(request.getCategory());
        }
        if (request.getIsAvailable() != null) {
            menuItem.setIsAvailable(request.getIsAvailable());
        }

        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return mapToResponse(updatedItem);
    }

    /**
     * Delete a menu item
     * 
     * @param ownerEmail Email of the restaurant owner (from JWT)
     * @param itemId ID of the menu item to delete
     * @throws IllegalArgumentException if restaurant or menu item not found
     */
    @Transactional
    public void deleteMenuItem(String ownerEmail, Long itemId) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found for email: " + ownerEmail));

        // Verify the menu item belongs to this restaurant
        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(itemId, restaurant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found or doesn't belong to your restaurant"));

        menuItemRepository.delete(menuItem);
    }

    /**
     * Toggle availability status of a menu item
     * 
     * @param ownerEmail Email of the restaurant owner (from JWT)
     * @param itemId ID of the menu item
     * @return Updated MenuItemResponse with new availability status
     */
    @Transactional
    public MenuItemResponse toggleAvailability(String ownerEmail, Long itemId) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found for email: " + ownerEmail));

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(itemId, restaurant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found or doesn't belong to your restaurant"));

        // Toggle availability
        menuItem.setIsAvailable(!menuItem.getIsAvailable());

        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return mapToResponse(updatedItem);
    }

    /**
     * Helper method to map MenuItem entity to MenuItemResponse DTO
     * 
     * @param menuItem The MenuItem entity
     * @return MenuItemResponse DTO
     */
    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(menuItem.getId());
        response.setName(menuItem.getName());
        response.setDescription(menuItem.getDescription());
        response.setPrice(menuItem.getPrice());
        response.setCategory(menuItem.getCategory());
        response.setIsAvailable(menuItem.getIsAvailable());
        response.setRestaurantId(menuItem.getRestaurant().getId());
        return response;
    }
}
