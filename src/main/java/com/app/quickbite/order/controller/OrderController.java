package com.app.quickbite.order.controller;

import com.app.quickbite.auth.entity.Restaurant;
import com.app.quickbite.auth.entity.User;
import com.app.quickbite.auth.repository.RestaurantRepository;
import com.app.quickbite.auth.repository.UserRepository;
import com.app.quickbite.order.dto.OrderResponse;
import com.app.quickbite.order.dto.OrderStatusUpdateRequest;
import com.app.quickbite.order.dto.PlaceOrderRequest;
import com.app.quickbite.order.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ORDER CONTROLLER - Customer order management endpoints.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Place a new order for the currently authenticated customer.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long customerId = resolveCurrentUserId(userDetails);
        OrderResponse response = orderService.placeOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Return the logged-in customer's order history.
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long customerId = resolveCurrentUserId(userDetails);
        return ResponseEntity.ok(orderService.getCustomerOrders(customerId));
    }

    /**
     * Update the status of an existing order.
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {

        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request.getNewStatus()));
    }

    /**
     * Fetch the authenticated restaurant owner's incoming orders with an optional status filter.
     */
    @GetMapping("/restaurant")
    public ResponseEntity<List<OrderResponse>> getRestaurantOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status) {

        Long userId = resolveCurrentUserId(userDetails);
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found for user: " + userId));

        return ResponseEntity.ok(orderService.getRestaurantOrders(restaurant.getId(), status));
    }

    /**
     * Resolve the authenticated user's database id from the current security context.
     */
    private Long resolveCurrentUserId(UserDetails userDetails) {
        String email = null;

        if (userDetails != null) {
            email = userDetails.getUsername();
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails principal) {
                email = principal.getUsername();
            } else if (authentication != null) {
                email = authentication.getName();
            }
        }

        if (email == null || email.isBlank()) {
            throw new EntityNotFoundException("Authenticated user not found");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));

        return user.getId();
    }
}