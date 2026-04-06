package com.app.quickbite.order.entity;

/**
 * ORDER STATUS - Lifecycle states for a customer order.
 */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}