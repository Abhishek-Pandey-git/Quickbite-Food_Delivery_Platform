package com.app.quickbite.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ORDER ITEM RESPONSE - Read model for an order line item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long menuItemId;
    private String menuItemName;
    private Integer quantity;
    private BigDecimal priceAtTimeOfOrder;
    private BigDecimal lineTotal;
}