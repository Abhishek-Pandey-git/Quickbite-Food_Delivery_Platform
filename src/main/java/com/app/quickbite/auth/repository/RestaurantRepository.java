package com.app.quickbite.auth.repository;

import com.app.quickbite.auth.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RESTAURANT REPOSITORY - Data Access Layer for Restaurant entity
 * 
 * Provides CRUD operations for Restaurant records.
 * Each Restaurant is linked to a User with role=RESTAURANT_OWNER.
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Find a restaurant by its associated user's ID
     * Used when we have the authenticated user and need to find their restaurant
     * 
     * @param userId The ID of the User who owns the restaurant
     * @return Optional containing the Restaurant if found
     */
    Optional<Restaurant> findByUserId(Long userId);

    /**
     * Find a restaurant by its associated user's email
     * Useful when we have the email from JWT token
     * 
     * @param email The email of the User who owns the restaurant
     * @return Optional containing the Restaurant if found
     */
    Optional<Restaurant> findByUserEmail(String email);

    /**
     * Find all approved restaurants in a specific city
     * Used for customer browsing - only shows approved restaurants
     * 
     * @param city The city to search in (partial match on address)
     * @return List of approved restaurants in the city
     */
    List<Restaurant> findByAddressContainingIgnoreCaseAndIsApprovedTrue(String city);

    /**
     * Find all approved and open restaurants in a city
     * 
     * @param city The city to search in
     * @return List of approved and open restaurants
     */
    List<Restaurant> findByAddressContainingIgnoreCaseAndIsApprovedTrueAndIsOpenTrue(String city);

    /**
     * Find all approved restaurants by cuisine type
     * 
     * @param cuisineType The cuisine type to filter by
     * @return List of approved restaurants with matching cuisine
     */
    List<Restaurant> findByCuisineTypeIgnoreCaseAndIsApprovedTrue(String cuisineType);

    /**
     * Find all approved restaurants (for listing all restaurants)
     * 
     * @return List of all approved restaurants
     */
    List<Restaurant> findByIsApprovedTrue();
}
