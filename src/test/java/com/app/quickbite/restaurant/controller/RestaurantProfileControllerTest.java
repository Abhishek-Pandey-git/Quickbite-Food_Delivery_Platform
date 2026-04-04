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
        // Sample profile response
        sampleProfileResponse = RestaurantProfileResponse.builder()
                .id(1L)
                .name("Test Restaurant")
                .description("A great place to eat")
                .address("123 Main St")
                .city("Bangalore")
                .cuisineType("Italian")
                .phoneNumber("+91-9876543210")
                .isOpen(true)
                .isApproved(true)
                .rating(4.5)
                .build();

        // Sample update request
        sampleUpdateRequest = RestaurantProfileUpdateRequest.builder()
                .name("Updated Restaurant")
                .description("Updated description")
                .address("456 New St")
                .city("Mumbai")
                .cuisineType("Mexican")
                .phoneNumber("+91-9876543211")
                .build();

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
        assertThat(response.getBody().getName()).isEqualTo("Test Restaurant");
        assertThat(response.getBody().getCity()).isEqualTo("Bangalore");
        assertThat(response.getBody().getCuisineType()).isEqualTo("Italian");
        assertThat(response.getBody().getIsOpen()).isTrue();
        assertThat(response.getBody().getRating()).isEqualTo(4.5);

        verify(restaurantProfileService).getProfile("restaurant@test.com");
    }

    @Test
    @DisplayName("Should update restaurant profile successfully")
    void shouldUpdateRestaurantProfileSuccessfully() {
        // Arrange
        RestaurantProfileResponse updatedResponse = RestaurantProfileResponse.builder()
                .id(1L)
                .name("Updated Restaurant")
                .description("Updated description")
                .address("456 New St")
                .city("Mumbai")
                .cuisineType("Mexican")
                .phoneNumber("+91-9876543211")
                .isOpen(true)
                .isApproved(true)
                .rating(4.5)
                .build();

        when(restaurantProfileService.updateProfile(anyString(), any(RestaurantProfileUpdateRequest.class)))
                .thenReturn(updatedResponse);

        // Act
        ResponseEntity<RestaurantProfileResponse> response = restaurantProfileController.updateProfile(mockUserDetails, sampleUpdateRequest);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Restaurant");
        assertThat(response.getBody().getCity()).isEqualTo("Mumbai");
        assertThat(response.getBody().getCuisineType()).isEqualTo("Mexican");

        verify(restaurantProfileService).updateProfile("restaurant@test.com", sampleUpdateRequest);
    }

    @Test
    @DisplayName("Should toggle open status successfully")
    void shouldToggleOpenStatusSuccessfully() {
        // Arrange
        RestaurantProfileResponse toggledResponse = RestaurantProfileResponse.builder()
                .id(1L)
                .name("Test Restaurant")
                .description("A great place to eat")
                .address("123 Main St")
                .city("Bangalore")
                .cuisineType("Italian")
                .phoneNumber("+91-9876543210")
                .isOpen(false) // Toggled to closed
                .isApproved(true)
                .rating(4.5)
                .build();

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
        // Arrange - Only updating name and city
        RestaurantProfileUpdateRequest partialRequest = RestaurantProfileUpdateRequest.builder()
                .name("Partially Updated Restaurant")
                .city("Delhi")
                .build();

        RestaurantProfileResponse updatedResponse = RestaurantProfileResponse.builder()
                .id(1L)
                .name("Partially Updated Restaurant")
                .description("A great place to eat") // Original description
                .address("123 Main St") // Original address
                .city("Delhi") // Updated
                .cuisineType("Italian") // Original cuisine
                .phoneNumber("+91-9876543210") // Original phone
                .isOpen(true)
                .isApproved(true)
                .rating(4.5)
                .build();

        when(restaurantProfileService.updateProfile(anyString(), any(RestaurantProfileUpdateRequest.class)))
                .thenReturn(updatedResponse);

        // Act
        ResponseEntity<RestaurantProfileResponse> response = restaurantProfileController.updateProfile(mockUserDetails, partialRequest);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Partially Updated Restaurant");
        assertThat(response.getBody().getCity()).isEqualTo("Delhi");
        assertThat(response.getBody().getDescription()).isEqualTo("A great place to eat");

        verify(restaurantProfileService).updateProfile("restaurant@test.com", partialRequest);
    }

    @Test
    @DisplayName("Should handle empty update requests")
    void shouldHandleEmptyUpdateRequests() {
        // Arrange - Empty update request (should keep original values)
        RestaurantProfileUpdateRequest emptyRequest = RestaurantProfileUpdateRequest.builder().build();

        when(restaurantProfileService.updateProfile(anyString(), any(RestaurantProfileUpdateRequest.class)))
                .thenReturn(sampleProfileResponse);

        // Act
        ResponseEntity<RestaurantProfileResponse> response = restaurantProfileController.updateProfile(mockUserDetails, emptyRequest);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Test Restaurant");

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

    @Test
    @DisplayName("Should pass null fields correctly in update request")
    void shouldPassNullFieldsCorrectlyInUpdateRequest() {
        // Arrange - Update request with some null fields
        RestaurantProfileUpdateRequest requestWithNulls = RestaurantProfileUpdateRequest.builder()
                .name("New Name")
                .description(null)
                .address(null)
                .city("New City")
                .cuisineType(null)
                .phoneNumber(null)
                .build();

        when(restaurantProfileService.updateProfile(anyString(), eq(requestWithNulls)))
                .thenReturn(sampleProfileResponse);

        // Act
        restaurantProfileController.updateProfile(mockUserDetails, requestWithNulls);

        // Assert
        verify(restaurantProfileService).updateProfile("restaurant@test.com", requestWithNulls);
    }
}