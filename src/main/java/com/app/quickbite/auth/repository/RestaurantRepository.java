package com.app.quickbite.auth.repository;

import com.app.quickbite.auth.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * RESTAURANT REPOSITORY - Data Access Layer for Restaurant entity
 * 
 * Provides CRUD operations for Restaurant records.
 * Each Restaurant is linked to a User with role=RESTAURANT_OWNER.
 * 
 * For now, we only need basic operations (save, findById, etc.)
 * which are inherited from JpaRepository.
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    // No custom methods needed for MVP
    // If we later need to find restaurants by user, cuisine type, etc.,
    // we can add methods like: List<Restaurant> findByCuisineType(String cuisineType);
}
