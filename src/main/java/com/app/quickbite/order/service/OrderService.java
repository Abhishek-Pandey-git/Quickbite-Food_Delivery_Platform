package com.app.quickbite.order.service;

import com.app.quickbite.auth.entity.Restaurant;
import com.app.quickbite.auth.entity.User;
import com.app.quickbite.auth.repository.RestaurantRepository;
import com.app.quickbite.auth.repository.UserRepository;
import com.app.quickbite.order.dto.OrderItemRequest;
import com.app.quickbite.order.dto.OrderItemResponse;
import com.app.quickbite.order.dto.OrderResponse;
import com.app.quickbite.order.dto.PlaceOrderRequest;
import com.app.quickbite.order.entity.Order;
import com.app.quickbite.order.entity.OrderItem;
import com.app.quickbite.order.entity.OrderStatus;
import com.app.quickbite.order.repository.OrderItemRepository;
import com.app.quickbite.order.repository.OrderRepository;
import com.app.quickbite.restaurant.entity.MenuItem;
import com.app.quickbite.restaurant.repository.MenuItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * ORDER SERVICE - Business logic for customer order placement and retrieval.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    /**
     * Place a new order for the customer using the frontend checkout payload.
     */
    @Transactional
    public OrderResponse placeOrder(Long customerId, PlaceOrderRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("At least one order item is required");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.PLACED);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setDeliveryAddress(request.getDeliveryAddress());

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> savedItems = request.getItems().stream()
                .map(itemRequest -> createOrderItem(savedOrder, restaurant, itemRequest))
                .collect(Collectors.toList());

        for (OrderItem orderItem : savedItems) {
            totalAmount = totalAmount.add(
                    orderItem.getPriceAtTimeOfOrder().multiply(BigDecimal.valueOf(orderItem.getQuantity()))
            );
        }

        savedOrder.setTotalAmount(totalAmount);
        orderRepository.save(savedOrder);
        orderItemRepository.saveAll(savedItems);

        return mapToOrderResponse(savedOrder, savedItems);
    }

    /**
     * Return all orders placed by a customer for the customer dashboard.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(order -> mapToOrderResponse(order, orderItemRepository.findByOrderId(order.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Update the order status for later restaurant or admin workflow steps.
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        OrderStatus status;
        try {
            status = OrderStatus.valueOf(newStatus.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new RuntimeException("Invalid order status: " + newStatus);
        }

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        return mapToOrderResponse(savedOrder, orderItemRepository.findByOrderId(savedOrder.getId()));
    }

    /**
     * Return incoming restaurant orders, optionally filtered by status.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(Long restaurantId, String status) {
        List<Order> orders = orderRepository.findByRestaurantId(restaurantId);
        OrderStatus desiredStatus = null;

        if (status != null && !status.isBlank()) {
            try {
                desiredStatus = OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
            } catch (Exception ex) {
                throw new RuntimeException("Invalid order status: " + status);
            }
        }

        OrderStatus finalDesiredStatus = desiredStatus;
        return orders.stream()
                .filter(order -> finalDesiredStatus == null || order.getStatus() == finalDesiredStatus)
                .map(order -> mapToOrderResponse(order, orderItemRepository.findByOrderId(order.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Create a single order item after validating the referenced menu item.
     */
    private OrderItem createOrderItem(Order order, Restaurant restaurant, OrderItemRequest itemRequest) {
        MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemRequest.getMenuItemId()));

        if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
            throw new RuntimeException("Menu item does not belong to the selected restaurant");
        }

        if (!Boolean.TRUE.equals(menuItem.getIsAvailable())) {
            throw new RuntimeException("Menu item is out of stock: " + menuItem.getName());
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setMenuItem(menuItem);
        orderItem.setQuantity(itemRequest.getQuantity());
        orderItem.setPriceAtTimeOfOrder(menuItem.getPrice());
        return orderItem;
    }

    /**
     * Map an order and its items into the frontend response model.
     */
    private OrderResponse mapToOrderResponse(Order order, List<OrderItem> orderItems) {
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(orderItem -> new OrderItemResponse(
                        orderItem.getMenuItem().getId(),
                        orderItem.getMenuItem().getName(),
                        orderItem.getQuantity(),
                        orderItem.getPriceAtTimeOfOrder(),
                        orderItem.getPriceAtTimeOfOrder().multiply(BigDecimal.valueOf(orderItem.getQuantity()))
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getRestaurant().getRestaurantName(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}