package com.app.quickbite.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESTAURANT ENTITY - A "Spoke" that extends User data
 * 
 * This table stores additional information specific to restaurant owners.
 * Each restaurant record is linked to exactly one User record (with role = RESTAURANT_OWNER).
 * 
 * The relationship: User (1) ← (1) Restaurant
 * When someone registers as a restaurant, we create BOTH a User record AND a Restaurant record.
 */
@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    
    /**
     * Primary key - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Foreign key reference to the User table
     * 
     * @ManyToOne: Many restaurants could theoretically link to one user (but in practice it's 1:1)
     * @JoinColumn: Specifies the foreign key column name in THIS table
     * 
     * When we save a Restaurant, this field must point to an existing User with role=RESTAURANT_OWNER
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Name of the restaurant (e.g., "Pizza Palace")
     */
    @Column(nullable = false)
    private String restaurantName;
    
    /**
     * Physical address of the restaurant
     */
    @Column(nullable = false)
    private String address;
    
    /**
     * Type of cuisine served (e.g., "Italian", "Chinese", "Fast Food")
     */
    @Column(nullable = false)
    private String cuisineType;
    
    /**
     * Admin approval flag
     * Default is true - restaurants are approved by default for MVP
     * 
     * @Column(columnDefinition = "boolean default true") ensures the DB column has a default value
     */
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean isApproved = true;

    @Column(nullable=false, columnDefinition = "boolean default true")
    private Boolean isOpen = true;
}
