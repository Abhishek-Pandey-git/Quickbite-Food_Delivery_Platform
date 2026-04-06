package com.app.quickbite.user.controller;

import com.app.quickbite.auth.entity.User;
import com.app.quickbite.auth.repository.UserRepository;
import com.app.quickbite.user.dto.UserProfileRequest;
import com.app.quickbite.user.dto.UserProfileResponse;
import com.app.quickbite.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * USER CONTROLLER - Customer profile endpoints.
 */
@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Get the profile for the currently authenticated user.
     */
    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveCurrentUserId(userDetails);
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    /**
     * Update the profile for the currently authenticated user.
     */
    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserProfileRequest request) {
        Long userId = resolveCurrentUserId(userDetails);
        return ResponseEntity.ok(userService.updateUserProfile(userId, request));
    }

    /**
     * Resolve the authenticated user's database id from the security context.
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
