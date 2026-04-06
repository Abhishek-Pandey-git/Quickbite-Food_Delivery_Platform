package com.app.quickbite.user.service;

import com.app.quickbite.auth.entity.User;
import com.app.quickbite.auth.repository.UserRepository;
import com.app.quickbite.user.dto.UserProfileRequest;
import com.app.quickbite.user.dto.UserProfileResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * USER SERVICE - Business logic for customer profile management.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Fetch the authenticated user's profile details.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return mapToResponse(user);
    }

    /**
     * Update the authenticated user's profile details.
     */
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDefaultDeliveryAddress() != null) {
            user.setDefaultDeliveryAddress(request.getDefaultDeliveryAddress());
        }

        return mapToResponse(userRepository.save(user));
    }

    /**
     * Map the domain entity to the profile response DTO.
     */
    private UserProfileResponse mapToResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getDefaultDeliveryAddress()
        );
    }
}
