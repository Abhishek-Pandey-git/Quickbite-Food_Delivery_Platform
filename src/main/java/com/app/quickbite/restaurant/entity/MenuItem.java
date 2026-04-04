package com.app.quickbite.restaurant.entity;

import com.app.quickbite.auth.entity.Restaurant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * MENU ITEM ENTITY - Represents a dish/item in a restaurant's menu
 * 
 * Each MenuItem belongs to exactly one Restaurant.
 * Restaurants can have multiple MenuItems (One-to-Many relationship).
 * 
 * The relationship: Restaurant (1) ← (Many) MenuItem
 */
@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    /**
     * Primary key - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key reference to the Restaurant table
     * 
     * @ManyToOne: Many menu items belong to one restaurant
     * @JoinColumn: Specifies the foreign key column name in THIS table
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    /**
     * Name of the menu item (e.g., "Margherita Pizza", "Chicken Tikka")
     */
    @Column(nullable = false)
    private String name;

    /**
     * Description of the dish (e.g., "Classic Italian pizza with fresh mozzarella and basil")
     */
    @Column(length = 500)
    private String description;

    /**
     * Price of the menu item
     * Using BigDecimal for precise monetary calculations
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Category of the dish (e.g., "Appetizers", "Main Course", "Desserts", "Beverages")
     */
    @Column(nullable = false)
    private String category;

    /**
     * Availability flag
     * Default is true - items are available unless marked otherwise
     * Useful for temporarily hiding out-of-stock items
     */
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean isAvailable = true;
}
