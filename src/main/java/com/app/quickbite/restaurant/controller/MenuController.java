package com.app.quickbite.restaurant.controller;

import com.app.quickbite.restaurant.dto.MenuItemRequest;
import com.app.quickbite.restaurant.dto.MenuItemResponse;
import com.app.quickbite.restaurant.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MENU CONTROLLER - REST API for Menu Item Management
 * 
 * Base Path: /api/restaurant/menu
 * 
 * Provides endpoints for:
 * - POST /api/restaurant/menu - Add new menu item
 * - GET /api/restaurant/menu - Get all menu items for the owner's restaurant
 * - GET /api/restaurant/menu/{id} - Get specific menu item
 * - PUT /api/restaurant/menu/{id} - Update menu item
 * - DELETE /api/restaurant/menu/{id} - Delete menu item
 * - PATCH /api/restaurant/menu/{id}/toggle-availability - Toggle item availability
 * 
 * All endpoints require authentication with RESTAURANT_OWNER role.
 * 
 * Public endpoint (for customers):
 * - GET /api/restaurant/{restaurantId}/menu - Get public menu for a restaurant
 */
@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * POST /api/restaurant/menu
     * 
     * Add a new menu item to the authenticated owner's restaurant.
     * 
     * @param userDetails Injected by Spring Security from JWT token
     * @param request DTO containing menu item details
     * @return Created MenuItemResponse with 201 status
     */
    @PostMapping("/menu")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MenuItemRequest request) {
        
        String ownerEmail = userDetails.getUsername();
        MenuItemResponse createdItem = menuService.addMenuItem(ownerEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    /**
     * GET /api/restaurant/menu
     * 
     * Get all menu items for the authenticated owner's restaurant.
     * Returns both available and unavailable items.
     * 
     * @param userDetails Injected by Spring Security from JWT token
     * @return List of all menu items
     */
    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemResponse>> getMenuItems(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String ownerEmail = userDetails.getUsername();
        List<MenuItemResponse> menuItems = menuService.getMenuItems(ownerEmail);
        return ResponseEntity.ok(menuItems);
    }

    /**
     * GET /api/restaurant/menu/category/{category}
     * 
     * Get menu items filtered by category.
     * 
     * @param userDetails Injected by Spring Security from JWT token
     * @param category The category to filter by
     * @return List of menu items in the specified category
     */
    @GetMapping("/menu/category/{category}")
    public ResponseEntity<List<MenuItemResponse>> getMenuItemsByCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String category) {
        
        String ownerEmail = userDetails.getUsername();
        List<MenuItemResponse> menuItems = menuService.getMenuItemsByCategory(ownerEmail, category);
        return ResponseEntity.ok(menuItems);
    }

    /**
     * PUT /api/restaurant/menu/{id}
     * 
     * Update an existing menu item.
     * 
     * @param userDetails Injected by Spring Security from JWT token
     * @param id ID of the menu item to update
     * @param request DTO containing updated details
     * @return Updated MenuItemResponse
     */
    @PutMapping("/menu/{id}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        
        String ownerEmail = userDetails.getUsername();
        MenuItemResponse updatedItem = menuService.updateMenuItem(ownerEmail, id, request);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * DELETE /api/restaurant/menu/{id}
     * 
     * Delete a menu item.
     * 
     * @param userDetails Injected by Spring Security from JWT token
     * @param id ID of the menu item to delete
     * @return Success message with 200 status
     */
    @DeleteMapping("/menu/{id}")
    public ResponseEntity<Map<String, String>> deleteMenuItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        String ownerEmail = userDetails.getUsername();
        menuService.deleteMenuItem(ownerEmail, id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Menu item deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/restaurant/menu/{id}/toggle-availability
     * 
     * Toggle the availability status of a menu item.
     * Useful for quickly marking items as sold out or back in stock.
     * 
     * @param userDetails Injected by Spring Security from JWT token
     * @param id ID of the menu item
     * @return Updated MenuItemResponse with new availability status
     */
    @PatchMapping("/menu/{id}/toggle-availability")
    public ResponseEntity<MenuItemResponse> toggleAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        String ownerEmail = userDetails.getUsername();
        MenuItemResponse updatedItem = menuService.toggleAvailability(ownerEmail, id);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * GET /api/restaurant/{restaurantId}/menu
     * 
     * PUBLIC ENDPOINT - Get menu for a specific restaurant.
     * Only returns AVAILABLE items.
     * Used by customers browsing restaurants.
     * 
     * @param restaurantId ID of the restaurant
     * @return List of available menu items
     */
    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<List<MenuItemResponse>> getPublicMenu(
            @PathVariable Long restaurantId) {
        
        List<MenuItemResponse> menuItems = menuService.getMenuItemsByRestaurantId(restaurantId);
        return ResponseEntity.ok(menuItems);
    }
}
