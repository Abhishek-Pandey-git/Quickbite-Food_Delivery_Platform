package com.app.quickbite.restaurant.service;

import com.app.quickbite.auth.entity.Restaurant;
import com.app.quickbite.auth.repository.RestaurantRepository;
import com.app.quickbite.restaurant.dto.RestaurantProfileResponse;
import com.app.quickbite.restaurant.dto.RestaurantProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RESTAURANT PROFILE SERVICE - Business Logic for Restaurant Profile Management
 * 
 * Handles operations related to restaurant profile:
 * - Getting profile information
 * - Updating profile details
 * - Managing open/closed status
 */
@Service
@RequiredArgsConstructor
public class RestaurantProfileService {

    private final RestaurantRepository restaurantRepository;

    /**
     * Get restaurant profile by owner's email
     * 
     * @param ownerEmail Email of the restaurant owner (from JWT)
     * @return RestaurantProfileResponse with all profile details
     * @throws IllegalArgumentException if no restaurant found for the email
     */
    public RestaurantProfileResponse getProfile(String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found for email: " + ownerEmail));

        return mapToResponse(restaurant);
    }

    /**
     * Update restaurant profile
     * 
     * @param ownerEmail Email of the restaurant owner (from JWT)
     * @param request DTO containing fields to update
     * @return Updated RestaurantProfileResponse
     * @throws IllegalArgumentException if no restaurant found for the email
     */
    @Transactional
    public RestaurantProfileResponse updateProfile(String ownerEmail, RestaurantProfileUpdateRequest request) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found for email: " + ownerEmail));

        // Update only non-null fields from the request
        if (request.getRestaurantName() != null) {
            restaurant.setRestaurantName(request.getRestaurantName());
        }
        if (request.getAddress() != null) {
            restaurant.setAddress(request.getAddress());
        }
        if (request.getCuisineType() != null) {
            restaurant.setCuisineType(request.getCuisineType());
        }
        if (request.getIsOpen() != null) {
            restaurant.setIsOpen(request.getIsOpen());
        }

        // Save and return updated profile
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(updatedRestaurant);
    }

    /**
     * Toggle restaurant open/closed status
     * 
     * @param ownerEmail Email of the restaurant owner (from JWT)
     * @return Updated RestaurantProfileResponse with new status
     */
    @Transactional
    public RestaurantProfileResponse toggleOpenStatus(String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found for email: " + ownerEmail));

        // Toggle the isOpen status
        restaurant.setIsOpen(!restaurant.getIsOpen());

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(updatedRestaurant);
    }

    /**
     * Helper method to map Restaurant entity to RestaurantProfileResponse DTO
     * 
     * @param restaurant The Restaurant entity
     * @return RestaurantProfileResponse DTO
     */
    private RestaurantProfileResponse mapToResponse(Restaurant restaurant) {
        RestaurantProfileResponse response = new RestaurantProfileResponse();
        response.setId(restaurant.getId());
        response.setRestaurantName(restaurant.getRestaurantName());
        response.setAddress(restaurant.getAddress());
        response.setCuisineType(restaurant.getCuisineType());
        response.setIsOpen(restaurant.getIsOpen());
        response.setIsApproved(restaurant.getIsApproved());

        // Get owner info from linked User entity
        if (restaurant.getUser() != null) {
            response.setOwnerName(restaurant.getUser().getFullName());
            response.setOwnerEmail(restaurant.getUser().getEmail());
            response.setOwnerPhone(restaurant.getUser().getPhoneNumber());
        }

        return response;
    }
}
