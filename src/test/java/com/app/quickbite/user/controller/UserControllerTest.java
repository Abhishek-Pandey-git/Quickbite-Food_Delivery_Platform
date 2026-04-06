package com.app.quickbite.user.controller;

import com.app.quickbite.auth.entity.User;
import com.app.quickbite.auth.repository.UserRepository;
import com.app.quickbite.user.dto.UserProfileRequest;
import com.app.quickbite.user.dto.UserProfileResponse;
import com.app.quickbite.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserController endpoints.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Controller Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private UserDetails mockUserDetails;
    private User mockDomainUser;
    private UserProfileRequest updateRequest;
    private UserProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        mockUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("customer@test.com")
                .password("password")
                .authorities("ROLE_CUSTOMER")
                .build();

        mockDomainUser = new User();
        mockDomainUser.setId(42L);
        mockDomainUser.setFullName("Test Customer");
        mockDomainUser.setEmail("customer@test.com");
        mockDomainUser.setPassword("encoded-password");
        mockDomainUser.setPhoneNumber("9876543210");
        mockDomainUser.setDefaultDeliveryAddress("123 Default Street");
        mockDomainUser.setRole("CUSTOMER");

        updateRequest = new UserProfileRequest(
                "Updated Customer",
                "9999999999",
                "456 New Delivery Street"
        );

        profileResponse = new UserProfileResponse(
                42L,
                "customer@test.com",
                "Test Customer",
                "9876543210",
                "123 Default Street"
        );
    }

    /**
     * Should return the current user's profile.
     */
    @Test
    @DisplayName("Should get user profile successfully")
    void shouldGetUserProfileSuccessfully() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(mockDomainUser));
        when(userService.getUserProfile(42L)).thenReturn(profileResponse);

        ResponseEntity<UserProfileResponse> response = userController.getProfile(mockUserDetails);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("customer@test.com");

        verify(userRepository).findByEmail("customer@test.com");
        verify(userService).getUserProfile(42L);
    }

    /**
     * Should update the current user's profile.
     */
    @Test
    @DisplayName("Should update user profile successfully")
    void shouldUpdateUserProfileSuccessfully() {
        UserProfileResponse updatedResponse = new UserProfileResponse(
                42L,
                "customer@test.com",
                "Updated Customer",
                "9999999999",
                "456 New Delivery Street"
        );

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(mockDomainUser));
        when(userService.updateUserProfile(eq(42L), any(UserProfileRequest.class))).thenReturn(updatedResponse);

        ResponseEntity<UserProfileResponse> response = userController.updateProfile(mockUserDetails, updateRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFullName()).isEqualTo("Updated Customer");

        verify(userRepository).findByEmail("customer@test.com");
        verify(userService).updateUserProfile(42L, updateRequest);
    }

    /**
     * Should fail when the user cannot be resolved from the current security context.
     */
    @Test
    @DisplayName("Should reject missing authenticated user")
    void shouldRejectMissingAuthenticatedUser() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.getProfile(mockUserDetails))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessage("Authenticated user not found");

        verify(userRepository).findByEmail("customer@test.com");
        verifyNoInteractions(userService);
    }
}
