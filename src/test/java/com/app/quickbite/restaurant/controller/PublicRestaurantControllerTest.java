package com.app.quickbite.restaurant.controller;

import com.app.quickbite.restaurant.dto.RestaurantDetailResponse;
import com.app.quickbite.restaurant.dto.RestaurantListingResponse;
import com.app.quickbite.restaurant.dto.MenuItemResponse;
import com.app.quickbite.restaurant.service.PublicRestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PublicRestaurantController endpoints
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Public Restaurant Controller Tests")
class PublicRestaurantControllerTest {

    @Mock
    private PublicRestaurantService publicRestaurantService;

    @InjectMocks
    private PublicRestaurantController publicRestaurantController;

    private List<RestaurantListingResponse> sampleRestaurants;
    private RestaurantDetailResponse sampleRestaurantDetail;

    @BeforeEach
    void setUp() {
        // Sample restaurant listings using constructor
        sampleRestaurants = Arrays.asList(
                new RestaurantListingResponse(1L, "Pizza Palace", "123 Main St", "Italian", true, 5),
                new RestaurantListingResponse(2L, "Burger Barn", "456 Food St", "American", false, 8),
                new RestaurantListingResponse(3L, "Curry House", "789 Spice Ave", "Indian", true, 12)
        );

        // Sample restaurant detail with menu
        List<MenuItemResponse> menuItems = Arrays.asList(
                new MenuItemResponse(1L, "Margherita Pizza", "Classic pizza with fresh mozzarella", 
                        new BigDecimal("12.99"), "Pizza", true, 1L),
                new MenuItemResponse(2L, "Pepperoni Pizza", "Pizza with pepperoni and cheese", 
                        new BigDecimal("14.99"), "Pizza", true, 1L)
        );

        sampleRestaurantDetail = new RestaurantDetailResponse(1L, "Pizza Palace", "123 Main St", "Italian", true, menuItems);
    }

    @Test
    @DisplayName("Should get all restaurants successfully")
    void shouldGetAllRestaurantsSuccessfully() {
        // Arrange
        when(publicRestaurantService.getAllRestaurants()).thenReturn(sampleRestaurants);

        // Act
        ResponseEntity<List<RestaurantListingResponse>> response = publicRestaurantController.getRestaurants(null, null, false);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody().get(0).getRestaurantName()).isEqualTo("Pizza Palace");
        assertThat(response.getBody().get(1).getRestaurantName()).isEqualTo("Burger Barn");
        assertThat(response.getBody().get(2).getRestaurantName()).isEqualTo("Curry House");

        verify(publicRestaurantService).getAllRestaurants();
        verify(publicRestaurantService, never()).getRestaurantsByCity(anyString());
        verify(publicRestaurantService, never()).getRestaurantsByCuisine(anyString());
    }

    @Test
    @DisplayName("Should filter restaurants by city successfully")
    void shouldFilterRestaurantsByCitySuccessfully() {
        // Arrange
        List<RestaurantListingResponse> bangaloreRestaurants = Arrays.asList(
                sampleRestaurants.get(0), // Pizza Palace
                sampleRestaurants.get(1)  // Burger Barn
        );
        when(publicRestaurantService.getRestaurantsByCity("Bangalore")).thenReturn(bangaloreRestaurants);

        // Act
        ResponseEntity<List<RestaurantListingResponse>> response = publicRestaurantController.getRestaurants("Bangalore", null, false);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
        // Note: RestaurantListingResponse doesn't have getCity() method

        verify(publicRestaurantService).getRestaurantsByCity("Bangalore");
        verify(publicRestaurantService, never()).getAllRestaurants();
        verify(publicRestaurantService, never()).getRestaurantsByCuisine(anyString());
    }

    @Test
    @DisplayName("Should filter restaurants by cuisine successfully")
    void shouldFilterRestaurantsByCuisineSuccessfully() {
        // Arrange
        List<RestaurantListingResponse> italianRestaurants = Arrays.asList(sampleRestaurants.get(0));
        when(publicRestaurantService.getRestaurantsByCuisine("Italian")).thenReturn(italianRestaurants);

        // Act
        ResponseEntity<List<RestaurantListingResponse>> response = publicRestaurantController.getRestaurants(null, "Italian", false);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getCuisineType()).isEqualTo("Italian");

        verify(publicRestaurantService).getRestaurantsByCuisine("Italian");
        verify(publicRestaurantService, never()).getAllRestaurants();
        verify(publicRestaurantService, never()).getRestaurantsByCity(anyString());
    }

    @Test
    @DisplayName("Should filter open restaurants by city successfully")
    void shouldFilterOpenRestaurantsByCitySuccessfully() {
        // Arrange
        List<RestaurantListingResponse> openBangaloreRestaurants = Arrays.asList(sampleRestaurants.get(0)); // Only Pizza Palace is open
        when(publicRestaurantService.getOpenRestaurantsByCity("Bangalore")).thenReturn(openBangaloreRestaurants);

        // Act
        ResponseEntity<List<RestaurantListingResponse>> response = publicRestaurantController.getRestaurants("Bangalore", null, true);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getRestaurantName()).isEqualTo("Pizza Palace");
        assertThat(response.getBody().get(0).getIsOpen()).isTrue();

        verify(publicRestaurantService).getOpenRestaurantsByCity("Bangalore");
        verify(publicRestaurantService, never()).getRestaurantsByCity("Bangalore");
        verify(publicRestaurantService, never()).getAllRestaurants();
    }

    @Test
    @DisplayName("Should get restaurant details with menu successfully")
    void shouldGetRestaurantDetailsWithMenuSuccessfully() {
        // Arrange
        when(publicRestaurantService.getRestaurantDetails(1L)).thenReturn(sampleRestaurantDetail);

        // Act
        ResponseEntity<RestaurantDetailResponse> response = publicRestaurantController.getRestaurantDetails(1L);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRestaurantName()).isEqualTo("Pizza Palace");
        // Note: RestaurantDetailResponse doesn't have getCity() or getPhoneNumber() methods
        assertThat(response.getBody().getMenuItems()).hasSize(2);
        assertThat(response.getBody().getMenuItems().get(0).getName()).isEqualTo("Margherita Pizza");
        assertThat(response.getBody().getMenuItems().get(1).getName()).isEqualTo("Pepperoni Pizza");

        verify(publicRestaurantService).getRestaurantDetails(1L);
    }

    @Test
    @DisplayName("Should handle empty city filter")
    void shouldHandleEmptyCityFilter() {
        // Arrange
        when(publicRestaurantService.getAllRestaurants()).thenReturn(sampleRestaurants);

        // Act
        ResponseEntity<List<RestaurantListingResponse>> response = publicRestaurantController.getRestaurants("", null, false);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(3);

        verify(publicRestaurantService).getAllRestaurants();
        verify(publicRestaurantService, never()).getRestaurantsByCity(anyString());
    }

    @Test
    @DisplayName("Should handle blank city filter")
    void shouldHandleBlankCityFilter() {
        // Arrange
        when(publicRestaurantService.getAllRestaurants()).thenReturn(sampleRestaurants);

        // Act
        ResponseEntity<List<RestaurantListingResponse>> response = publicRestaurantController.getRestaurants("   ", null, false);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(3);

        verify(publicRestaurantService).getAllRestaurants();
        verify(publicRestaurantService, never()).getRestaurantsByCity(anyString());
    }

    @Test
    @DisplayName("Should handle empty cuisine filter")
    void shouldHandleEmptyCuisineFilter() {
        // Arrange
        when(publicRestaurantService.getAllRestaurants()).thenReturn(sampleRestaurants);

        // Act
        ResponseEntity<List<RestaurantListingResponse>> response = publicRestaurantController.getRestaurants(null, "", false);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(3);

        verify(publicRestaurantService).getAllRestaurants();
        verify(publicRestaurantService, never()).getRestaurantsByCuisine(anyString());
    }

    @Test
    @DisplayName("Should default openOnly to false")
    void shouldDefaultOpenOnlyToFalse() {
        // Arrange
        when(publicRestaurantService.getRestaurantsByCity("Bangalore")).thenReturn(Arrays.asList(sampleRestaurants.get(0)));

        // Act - Default openOnly to false
        ResponseEntity<List<RestaurantListingResponse>> response = publicRestaurantController.getRestaurants("Bangalore", null, false);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        verify(publicRestaurantService).getRestaurantsByCity("Bangalore");
        verify(publicRestaurantService, never()).getOpenRestaurantsByCity(anyString());
    }

    @Test
    @DisplayName("Should return empty list when no restaurants found")
    void shouldReturnEmptyListWhenNoRestaurantsFound() {
        // Arrange
        when(publicRestaurantService.getRestaurantsByCity("NonexistentCity")).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<RestaurantListingResponse>> response = publicRestaurantController.getRestaurants("NonexistentCity", null, false);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();

        verify(publicRestaurantService).getRestaurantsByCity("NonexistentCity");
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void shouldHandleServiceExceptionsGracefully() {
        // Arrange
        when(publicRestaurantService.getRestaurantDetails(999L))
                .thenThrow(new RuntimeException("Restaurant not found"));

        // Act & Assert
        assertThatThrownBy(() -> publicRestaurantController.getRestaurantDetails(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Restaurant not found");

        verify(publicRestaurantService).getRestaurantDetails(999L);
    }

    @Test
    @DisplayName("Should handle multiple query parameters correctly")
    void shouldHandleMultipleQueryParametersCorrectly() {
        // City parameter takes precedence over cuisine when both are present
        // Arrange
        when(publicRestaurantService.getRestaurantsByCity("Bangalore")).thenReturn(Arrays.asList(sampleRestaurants.get(0)));

        // Act
        ResponseEntity<List<RestaurantListingResponse>> response = publicRestaurantController.getRestaurants("Bangalore", "Italian", false);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        verify(publicRestaurantService).getRestaurantsByCity("Bangalore");
        verify(publicRestaurantService, never()).getRestaurantsByCuisine("Italian");
    }

    @Test
    @DisplayName("Should call correct service method based on parameters")
    void shouldCallCorrectServiceMethodBasedOnParameters() {
        // Test 1: Only city parameter
        when(publicRestaurantService.getRestaurantsByCity("Mumbai")).thenReturn(Arrays.asList());
        publicRestaurantController.getRestaurants("Mumbai", null, false);
        verify(publicRestaurantService).getRestaurantsByCity("Mumbai");

        // Test 2: Only cuisine parameter
        reset(publicRestaurantService);
        when(publicRestaurantService.getRestaurantsByCuisine("Chinese")).thenReturn(Arrays.asList());
        publicRestaurantController.getRestaurants(null, "Chinese", false);
        verify(publicRestaurantService).getRestaurantsByCuisine("Chinese");

        // Test 3: No parameters
        reset(publicRestaurantService);
        when(publicRestaurantService.getAllRestaurants()).thenReturn(Arrays.asList());
        publicRestaurantController.getRestaurants(null, null, false);
        verify(publicRestaurantService).getAllRestaurants();
    }
}