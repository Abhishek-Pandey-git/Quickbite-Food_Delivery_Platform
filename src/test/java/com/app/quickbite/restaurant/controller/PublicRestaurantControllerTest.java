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
        // Sample restaurant listings
        sampleRestaurants = Arrays.asList(
                RestaurantListingResponse.builder()
                        .id(1L)
                        .name("Pizza Palace")
                        .description("Best pizzas in town")
                        .address("123 Main St")
                        .city("Bangalore")
                        .cuisineType("Italian")
                        .isOpen(true)
                        .rating(4.5)
                        .build(),
                RestaurantListingResponse.builder()
                        .id(2L)
                        .name("Burger Barn")
                        .description("Gourmet burgers and fries")
                        .address("456 Food St")
                        .city("Bangalore")
                        .cuisineType("American")
                        .isOpen(false)
                        .rating(4.2)
                        .build(),
                RestaurantListingResponse.builder()
                        .id(3L)
                        .name("Curry House")
                        .description("Authentic Indian cuisine")
                        .address("789 Spice Ave")
                        .city("Mumbai")
                        .cuisineType("Indian")
                        .isOpen(true)
                        .rating(4.7)
                        .build()
        );

        // Sample restaurant detail with menu
        List<MenuItemResponse> menuItems = Arrays.asList(
                MenuItemResponse.builder()
                        .id(1L)
                        .name("Margherita Pizza")
                        .description("Classic pizza with fresh mozzarella")
                        .price(new BigDecimal("12.99"))
                        .category("Pizza")
                        .isVegetarian(true)
                        .isAvailable(true)
                        .restaurantId(1L)
                        .build(),
                MenuItemResponse.builder()
                        .id(2L)
                        .name("Pepperoni Pizza")
                        .description("Pizza with pepperoni and cheese")
                        .price(new BigDecimal("14.99"))
                        .category("Pizza")
                        .isVegetarian(false)
                        .isAvailable(true)
                        .restaurantId(1L)
                        .build()
        );

        sampleRestaurantDetail = RestaurantDetailResponse.builder()
                .id(1L)
                .name("Pizza Palace")
                .description("Best pizzas in town")
                .address("123 Main St")
                .city("Bangalore")
                .cuisineType("Italian")
                .phoneNumber("+91-9876543210")
                .isOpen(true)
                .rating(4.5)
                .menuItems(menuItems)
                .build();
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
        assertThat(response.getBody().get(0).getName()).isEqualTo("Pizza Palace");
        assertThat(response.getBody().get(1).getName()).isEqualTo("Burger Barn");
        assertThat(response.getBody().get(2).getName()).isEqualTo("Curry House");

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
        assertThat(response.getBody().get(0).getCity()).isEqualTo("Bangalore");
        assertThat(response.getBody().get(1).getCity()).isEqualTo("Bangalore");

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
        assertThat(response.getBody().get(0).getName()).isEqualTo("Pizza Palace");
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
        assertThat(response.getBody().getName()).isEqualTo("Pizza Palace");
        assertThat(response.getBody().getCity()).isEqualTo("Bangalore");
        assertThat(response.getBody().getPhoneNumber()).isEqualTo("+91-9876543210");
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

        // Act - No openOnly parameter should default to false (null gets converted to false by Spring)
        ResponseEntity<List<RestaurantListingResponse>> response = publicRestaurantController.getRestaurants("Bangalore", null, null);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        // Since openOnly is null, it should be converted to false by Spring's default value
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