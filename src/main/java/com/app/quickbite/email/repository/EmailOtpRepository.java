package com.app.quickbite.email.repository;

import com.app.quickbite.email.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * EMAIL OTP REPOSITORY - Database operations for OTP management
 */
@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    
    /**
     * Find the latest active OTP for an email
     */
    @Query("SELECT o FROM EmailOtp o WHERE o.email = :email AND o.verified = false ORDER BY o.createdAt DESC LIMIT 1")
    Optional<EmailOtp> findLatestActiveOtpByEmail(@Param("email") String email);
    
    /**
     * Find valid OTP (not expired, not verified, not max attempts)
     */
    @Query("SELECT o FROM EmailOtp o WHERE o.email = :email AND o.otpCode = :otpCode AND o.verified = false AND o.expiresAt > :now AND o.attempts < 3")
    Optional<EmailOtp> findValidOtp(@Param("email") String email, @Param("otpCode") String otpCode, @Param("now") LocalDateTime now);
    
    /**
     * Count OTPs sent to an email in the last hour (rate limiting)
     */
    @Query("SELECT COUNT(o) FROM EmailOtp o WHERE o.email = :email AND o.createdAt > :oneHourAgo")
    Long countOtpsSentInLastHour(@Param("email") String email, @Param("oneHourAgo") LocalDateTime oneHourAgo);
    
    /**
     * Delete expired OTPs (cleanup job)
     */
    List<EmailOtp> findByExpiresAtBefore(LocalDateTime dateTime);
}