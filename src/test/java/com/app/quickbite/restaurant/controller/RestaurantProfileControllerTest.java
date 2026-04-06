package com.app.quickbite.restaurant.controller;

import com.app.quickbite.restaurant.dto.RestaurantProfileResponse;
import com.app.quickbite.restaurant.dto.RestaurantProfileUpdateRequest;
import com.app.quickbite.restaurant.service.RestaurantProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RestaurantProfileController endpoints
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Restaurant Profile Controller Tests")
class RestaurantProfileControllerTest {

    @Mock
    private RestaurantProfileService restaurantProfileService;

    @InjectMocks
    private RestaurantProfileController restaurantProfileController;

    private RestaurantProfileResponse sampleProfileResponse;
    private RestaurantProfileUpdateRequest sampleUpdateRequest;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        // Sample profile response using constructor
        sampleProfileResponse = new RestaurantProfileResponse(
                1L,
                "Test Restaurant",
                "123 Main St",
                "Italian",
                true,
                true,
                "John Doe",
                "john@test.com",
                "+91-9876543210"
        );

        // Sample update request
        sampleUpdateRequest = new RestaurantProfileUpdateRequest(
                "Updated Restaurant",
                "456 New St",
                "Mexican",
                false
        );

        // Mock authenticated user
        mockUserDetails = User.builder()
                .username("restaurant@test.com")
                .password("password")
                .authorities("ROLE_RESTAURANT_OWNER")
                .build();
    }

    @Test
    @DisplayName("Should get restaurant profile successfully")
    void shouldGetRestaurantProfileSuccessfully() {
        // Arrange
        when(restaurantProfileService.getProfile(anyString()))
                .thenReturn(sampleProfileResponse);

        // Act
        ResponseEntity<RestaurantProfileResponse> response = restaurantProfileController.getProfile(mockUserDetails);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRestaurantName()).isEqualTo("Test Restaurant");
        assertThat(response.getBody().getCuisineType()).isEqualTo("Italian");
        assertThat(response.getBody().getIsOpen()).isTrue();

        verify(restaurantProfileService).getProfile("restaurant@test.com");
    }

    @Test
    @DisplayName("Should update restaurant profile successfully")
    void shouldUpdateRestaurantProfileSuccessfully() {
        // Arrange
        RestaurantProfileResponse updatedResponse = new RestaurantProfileResponse(
                1L,
                "Updated Restaurant",
                "456 New St",
                "Mexican",
                false,
                true,
                "John Doe",
                "john@test.com",
                "+91-9876543210"
        );

        when(restaurantProfileService.updateProfile(anyString(), any(RestaurantProfileUpdateRequest.class)))
                .thenReturn(updatedResponse);

        // Act
        ResponseEntity<RestaurantProfileResponse> response = restaurantProfileController.updateProfile(mockUserDetails, sampleUpdateRequest);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRestaurantName()).isEqualTo("Updated Restaurant");
        assertThat(response.getBody().getCuisineType()).isEqualTo("Mexican");
        assertThat(response.getBody().getIsOpen()).isFalse();

        verify(restaurantProfileService).updateProfile("restaurant@test.com", sampleUpdateRequest);
    }

    @Test
    @DisplayName("Should toggle open status successfully")
    void shouldToggleOpenStatusSuccessfully() {
        // Arrange
        RestaurantProfileResponse toggledResponse = new RestaurantProfileResponse(
                1L,
                "Test Restaurant",
                "123 Main St",
                "Italian",
                false, // Toggled to closed
                true,
                "John Doe",
                "john@test.com",
                "+91-9876543210"
        );

        when(restaurantProfileService.toggleOpenStatus(anyString()))
                .thenReturn(toggledResponse);

        // Act
        ResponseEntity<RestaurantProfileResponse> response = restaurantProfileController.toggleOpenStatus(mockUserDetails);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsOpen()).isFalse();

        verify(restaurantProfileService).toggleOpenStatus("restaurant@test.com");
    }

    @Test
    @DisplayName("Should handle partial update requests")
    void shouldHandlePartialUpdateRequests() {
        // Arrange - Only updating name
        RestaurantProfileUpdateRequest partialRequest = new RestaurantProfileUpdateRequest(
                "Partially Updated Restaurant",
                null,
                null,
                null
        );

        RestaurantProfileResponse updatedResponse = new RestaurantProfileResponse(
                1L,
                "Partially Updated Restaurant",
                "123 Main St", // Original address
                "Italian", // Original cuisine
                true,
                true,
                "John Doe",
                "john@test.com",
                "+91-9876543210"
        );

        when(restaurantProfileService.updateProfile(anyString(), any(RestaurantProfileUpdateRequest.class)))
                .thenReturn(updatedResponse);

        // Act
        ResponseEntity<RestaurantProfileResponse> response = restaurantProfileController.updateProfile(mockUserDetails, partialRequest);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRestaurantName()).isEqualTo("Partially Updated Restaurant");
        assertThat(response.getBody().getAddress()).isEqualTo("123 Main St");

        verify(restaurantProfileService).updateProfile("restaurant@test.com", partialRequest);
    }

    @Test
    @DisplayName("Should handle empty update requests")
    void shouldHandleEmptyUpdateRequests() {
        // Arrange - Empty update request (should keep original values)
        RestaurantProfileUpdateRequest emptyRequest = new RestaurantProfileUpdateRequest(null, null, null, null);

        when(restaurantProfileService.updateProfile(anyString(), any(RestaurantProfileUpdateRequest.class)))
                .thenReturn(sampleProfileResponse);

        // Act
        ResponseEntity<RestaurantProfileResponse> response = restaurantProfileController.updateProfile(mockUserDetails, emptyRequest);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRestaurantName()).isEqualTo("Test Restaurant");

        verify(restaurantProfileService).updateProfile("restaurant@test.com", emptyRequest);
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void shouldHandleServiceExceptionsGracefully() {
        // Arrange - Service throws exception
        when(restaurantProfileService.getProfile(anyString()))
                .thenThrow(new RuntimeException("Profile not found"));

        // Act & Assert
        assertThatThrownBy(() -> restaurantProfileController.getProfile(mockUserDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Profile not found");

        verify(restaurantProfileService).getProfile("restaurant@test.com");
    }

    @Test
    @DisplayName("Should extract correct username from UserDetails")
    void shouldExtractCorrectUsernameFromUserDetails() {
        // Arrange
        when(restaurantProfileService.getProfile(eq("restaurant@test.com"))).thenReturn(sampleProfileResponse);

        // Act
        restaurantProfileController.getProfile(mockUserDetails);

        // Assert
        verify(restaurantProfileService).getProfile("restaurant@test.com");
    }
}