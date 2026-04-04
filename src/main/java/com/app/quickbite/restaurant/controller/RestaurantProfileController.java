package com.app.quickbite.restaurant.controller;

import com.app.quickbite.restaurant.dto.RestaurantProfileResponse;
import com.app.quickbite.restaurant.dto.RestaurantProfileUpdateRequest;
import com.app.quickbite.restaurant.service.RestaurantProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * RESTAURANT PROFILE CONTROLLER - REST API for Restaurant Profile Management
 * 
 * Base Path: /api/restaurant/profile
 * 
 * Provides endpoints for:
 * - GET /api/restaurant/profile - Get current restaurant's profile
 * - PUT /api/restaurant/profile - Update restaurant profile
 * - PATCH /api/restaurant/profile/toggle-status - Toggle open/closed status
 * 
 * All endpoints require authentication with RESTAURANT_OWNER role.
 */
@RestController
@RequestMapping("/api/restaurant/profile")
@RequiredArgsConstructor
public class RestaurantProfileController {

    private final RestaurantProfileService restaurantProfileService;

    /**
     * GET /api/restaurant/profile
     * 
     * Get the profile of the authenticated restaurant owner's restaurant.
     * 
     * @param userDetails Injected by Spring Security from JWT token
     * @return RestaurantProfileResponse with all profile details
     */
    @GetMapping
    public ResponseEntity<RestaurantProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String ownerEmail = userDetails.getUsername();
        RestaurantProfileResponse profile = restaurantProfileService.getProfile(ownerEmail);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/restaurant/profile
     * 
     * Update the restaurant profile. Only provided fields will be updated.
     * 
     * @param userDetails Injected by Spring Security from JWT token
     * @param request DTO containing fields to update
     * @return Updated RestaurantProfileResponse
     */
    @PutMapping
    public ResponseEntity<RestaurantProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody RestaurantProfileUpdateRequest request) {
        
        String ownerEmail = userDetails.getUsername();
        RestaurantProfileResponse updatedProfile = restaurantProfileService.updateProfile(ownerEmail, request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * PATCH /api/restaurant/profile/toggle-status
     * 
     * Quick toggle for open/closed status.
     * Useful for mobile apps to quickly change availability.
     * 
     * @param userDetails Injected by Spring Security from JWT token
     * @return Updated RestaurantProfileResponse with new status
     */
    @PatchMapping("/toggle-status")
    public ResponseEntity<RestaurantProfileResponse> toggleOpenStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String ownerEmail = userDetails.getUsername();
        RestaurantProfileResponse updatedProfile = restaurantProfileService.toggleOpenStatus(ownerEmail);
        return ResponseEntity.ok(updatedProfile);
    }
}
