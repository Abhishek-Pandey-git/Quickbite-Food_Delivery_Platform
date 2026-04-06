package com.app.quickbite.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DELIVERY AGENT ENTITY - Another "Spoke" that extends User data
 * 
 * This table stores additional information specific to delivery agents.
 * Each delivery agent record is linked to exactly one User record (with role = DELIVERY_AGENT).
 * 
 * The relationship: User (1) ← (1) DeliveryAgent
 */
@Entity
@Table(name = "delivery_agents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAgent {
    
    /**
     * Primary key - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Foreign key reference to the User table
     * 
     * Same pattern as Restaurant - links to a User with role=DELIVERY_AGENT
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Vehicle registration number (e.g., "AB12 CD34" or "KA-01-AB-1234")
     * Used for tracking and verification purposes
     */
    @Column(nullable = false)
    private String vehicleNumber;

    /**
     * Address which will store city in india
     */

    @Column(nullable = false)
    private String address;
    
    /**
     * Verification status flag
     * Default is false - delivery agents must be verified (background check, documents, etc.)
     * before they can start accepting delivery jobs
     */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isVerified = false;
}
