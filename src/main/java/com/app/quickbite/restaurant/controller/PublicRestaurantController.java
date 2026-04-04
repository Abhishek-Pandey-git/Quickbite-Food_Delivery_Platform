package com.app.quickbite.restaurant.controller;

import com.app.quickbite.restaurant.dto.RestaurantDetailResponse;
import com.app.quickbite.restaurant.dto.RestaurantListingResponse;
import com.app.quickbite.restaurant.service.PublicRestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PUBLIC RESTAURANT CONTROLLER - Customer-facing endpoints
 * 
 * Base Path: /api/restaurants (note: plural for REST convention)
 * 
 * These endpoints are PUBLIC - no authentication required.
 * Used by customers to browse restaurants before ordering.
 * 
 * Endpoints:
 * - GET /api/restaurants                     - Get all restaurants
 * - GET /api/restaurants?city=Bangalore      - Filter by city
 * - GET /api/restaurants?cuisine=Italian     - Filter by cuisine
 * - GET /api/restaurants?city=X&openOnly=true - Only open restaurants
 * - GET /api/restaurants/{id}                - Get restaurant details + menu
 */
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class PublicRestaurantController {

    private final PublicRestaurantService publicRestaurantService;

    /**
     * GET /api/restaurants
     * 
     * Get list of restaurants with optional filters.
     * 
     * Query Parameters:
     * - city (optional): Filter by city name
     * - cuisine (optional): Filter by cuisine type
     * - openOnly (optional): If true, only return open restaurants
     * 
     * @return List of restaurants matching the filters
     */
    @GetMapping
    public ResponseEntity<List<RestaurantListingResponse>> getRestaurants(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false, defaultValue = "false") Boolean openOnly) {
        
        List<RestaurantListingResponse> restaurants;

        if (city != null && !city.isBlank()) {
            // Filter by city
            if (openOnly) {
                restaurants = publicRestaurantService.getOpenRestaurantsByCity(city);
            } else {
                restaurants = publicRestaurantService.getRestaurantsByCity(city);
            }
        } else if (cuisine != null && !cuisine.isBlank()) {
            // Filter by cuisine type
            restaurants = publicRestaurantService.getRestaurantsByCuisine(cuisine);
        } else {
            // No filter - return all
            restaurants = publicRestaurantService.getAllRestaurants();
        }

        return ResponseEntity.ok(restaurants);
    }

    /**
     * GET /api/restaurants/{id}
     * 
     * Get detailed view of a restaurant including its menu.
     * Only returns available menu items.
     * 
     * @param id Restaurant ID
     * @return Restaurant details with menu
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDetailResponse> getRestaurantDetails(
            @PathVariable Long id) {
        
        RestaurantDetailResponse restaurant = publicRestaurantService.getRestaurantDetails(id);
        return ResponseEntity.ok(restaurant);
    }
}
