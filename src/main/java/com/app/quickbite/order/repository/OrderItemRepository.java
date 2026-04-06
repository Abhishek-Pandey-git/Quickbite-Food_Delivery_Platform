package com.app.quickbite.order.repository;

import com.app.quickbite.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ORDER ITEM REPOSITORY - Data access for order line items.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all items associated with a given order.
     */
    List<OrderItem> findByOrderId(Long orderId);
}