package com.app.quickbite.restaurant.service;

import com.app.quickbite.auth.entity.Restaurant;
import com.app.quickbite.auth.repository.RestaurantRepository;
import com.app.quickbite.restaurant.dto.MenuItemResponse;
import com.app.quickbite.restaurant.dto.RestaurantDetailResponse;
import com.app.quickbite.restaurant.dto.RestaurantListingResponse;
import com.app.quickbite.restaurant.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PUBLIC RESTAURANT SERVICE - For Customer Browsing
 * 
 * Handles public operations for customers:
 * - Listing restaurants by city
 * - Getting restaurant details
 * - Filtering by cuisine type
 */
@Service
@RequiredArgsConstructor
public class PublicRestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    /**
     * Get all approved restaurants in a city
     * 
     * @param city City name to search for
     * @return List of restaurants in that city
     */
    public List<RestaurantListingResponse> getRestaurantsByCity(String city) {
        List<Restaurant> restaurants = restaurantRepository
                .findByAddressContainingIgnoreCaseAndIsApprovedTrue(city);
        
        return restaurants.stream()
                .map(this::mapToListingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all approved and currently open restaurants in a city
     * 
     * @param city City name to search for
     * @return List of open restaurants in that city
     */
    public List<RestaurantListingResponse> getOpenRestaurantsByCity(String city) {
        List<Restaurant> restaurants = restaurantRepository
                .findByAddressContainingIgnoreCaseAndIsApprovedTrueAndIsOpenTrue(city);
        
        return restaurants.stream()
                .map(this::mapToListingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all approved restaurants by cuisine type
     * 
     * @param cuisineType Cuisine type to filter by
     * @return List of restaurants with matching cuisine
     */
    public List<RestaurantListingResponse> getRestaurantsByCuisine(String cuisineType) {
        List<Restaurant> restaurants = restaurantRepository
                .findByCuisineTypeIgnoreCaseAndIsApprovedTrue(cuisineType);
        
        return restaurants.stream()
                .map(this::mapToListingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all approved restaurants (no filter)
     * 
     * @return List of all approved restaurants
     */
    public List<RestaurantListingResponse> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findByIsApprovedTrue();
        
        return restaurants.stream()
                .map(this::mapToListingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get detailed restaurant view with menu
     * 
     * @param restaurantId ID of the restaurant
     * @return Restaurant details with menu items
     * @throws IllegalArgumentException if restaurant not found or not approved
     */
    public RestaurantDetailResponse getRestaurantDetails(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        // Only show approved restaurants to customers
        if (!restaurant.getIsApproved()) {
            throw new IllegalArgumentException("Restaurant not found");
        }

        // Get available menu items
        List<MenuItemResponse> menuItems = menuItemRepository
                .findByRestaurantIdAndIsAvailable(restaurantId, true)
                .stream()
                .map(item -> {
                    MenuItemResponse response = new MenuItemResponse();
                    response.setId(item.getId());
                    response.setName(item.getName());
                    response.setDescription(item.getDescription());
                    response.setPrice(item.getPrice());
                    response.setCategory(item.getCategory());
                    response.setIsAvailable(item.getIsAvailable());
                    response.setRestaurantId(restaurantId);
                    return response;
                })
                .collect(Collectors.toList());

        RestaurantDetailResponse response = new RestaurantDetailResponse();
        response.setId(restaurant.getId());
        response.setRestaurantName(restaurant.getRestaurantName());
        response.setAddress(restaurant.getAddress());
        response.setCuisineType(restaurant.getCuisineType());
        response.setIsOpen(restaurant.getIsOpen());
        response.setMenuItems(menuItems);

        return response;
    }

    /**
     * Helper method to map Restaurant entity to listing response
     */
    private RestaurantListingResponse mapToListingResponse(Restaurant restaurant) {
        int menuItemCount = menuItemRepository
                .findByRestaurantIdAndIsAvailable(restaurant.getId(), true)
                .size();

        RestaurantListingResponse response = new RestaurantListingResponse();
        response.setId(restaurant.getId());
        response.setRestaurantName(restaurant.getRestaurantName());
        response.setAddress(restaurant.getAddress());
        response.setCuisineType(restaurant.getCuisineType());
        response.setIsOpen(restaurant.getIsOpen());
        response.setMenuItemCount(menuItemCount);
        return response;
    }
}
