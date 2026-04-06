package com.app.quickbite.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ORDER RESPONSE - Read model returned to the frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long orderId;
    private String restaurantName;
    private String status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}