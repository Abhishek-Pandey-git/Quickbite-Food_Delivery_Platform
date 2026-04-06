package com.app.quickbite.order.repository;

import com.app.quickbite.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ORDER REPOSITORY - Data access for customer orders.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders placed by a specific customer.
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Find all orders received by a specific restaurant.
     */
    List<Order> findByRestaurantId(Long restaurantId);
}