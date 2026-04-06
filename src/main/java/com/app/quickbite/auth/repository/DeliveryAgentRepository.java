package com.app.quickbite.auth.repository;

import com.app.quickbite.auth.entity.DeliveryAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * DELIVERY AGENT REPOSITORY - Data Access Layer for DeliveryAgent entity
 * 
 * Provides CRUD operations for DeliveryAgent records.
 * Each DeliveryAgent is linked to a User with role=DELIVERY_AGENT.
 * 
 * For now, we only need basic operations which are inherited from JpaRepository.
 */
@Repository
public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, Long> {
    // No custom methods needed for MVP
    // Future enhancements could include:
    // - List<DeliveryAgent> findByIsVerified(Boolean isVerified);
    // - Optional<DeliveryAgent> findByVehicleNumber(String vehicleNumber);
}
