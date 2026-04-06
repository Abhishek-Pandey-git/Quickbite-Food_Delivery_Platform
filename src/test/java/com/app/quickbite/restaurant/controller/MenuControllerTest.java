package com.app.quickbite.restaurant.controller;

import com.app.quickbite.restaurant.dto.MenuItemRequest;
import com.app.quickbite.restaurant.dto.MenuItemResponse;
import com.app.quickbite.restaurant.service.MenuService;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MenuController endpoints
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Menu Controller Tests")
class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    private MenuItemRequest sampleMenuItemRequest;
    private MenuItemResponse sampleMenuItemResponse;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        // Sample request object
        sampleMenuItemRequest = new MenuItemRequest(
                "Margherita Pizza",
                "Classic pizza with fresh mozzarella and basil",
                new BigDecimal("12.99"),
                "Pizza",
                true
        );

        // Sample response object
        sampleMenuItemResponse = new MenuItemResponse(
                1L,
                "Margherita Pizza",
                "Classic pizza with fresh mozzarella and basil",
                new BigDecimal("12.99"),
                "Pizza",
                true,
                1L
        );

        // Mock authenticated user
        mockUserDetails = User.builder()
                .username("restaurant@test.com")
                .password("password")
                .authorities("ROLE_RESTAURANT_OWNER")
                .build();
    }

    @Test
    @DisplayName("Should add menu item successfully")
    void shouldAddMenuItemSuccessfully() {
        // Arrange
        when(menuService.addMenuItem(anyString(), any(MenuItemRequest.class)))
                .thenReturn(sampleMenuItemResponse);

        // Act
        ResponseEntity<MenuItemResponse> response = menuController.addMenuItem(mockUserDetails, sampleMenuItemRequest);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Margherita Pizza");
        assertThat(response.getBody().getPrice()).isEqualTo(new BigDecimal("12.99"));
        assertThat(response.getBody().getCategory()).isEqualTo("Pizza");
        assertThat(response.getBody().getIsAvailable()).isTrue();

        verify(menuService).addMenuItem("restaurant@test.com", sampleMenuItemRequest);
    }

    @Test
    @DisplayName("Should get all menu items successfully")
    void shouldGetAllMenuItemsSuccessfully() {
        // Arrange
        List<MenuItemResponse> menuItems = Arrays.asList(
                sampleMenuItemResponse,
                new MenuItemResponse(
                        2L,
                        "Pepperoni Pizza",
                        "Pizza with pepperoni and cheese",
                        new BigDecimal("14.99"),
                        "Pizza",
                        true,
                        1L
                )
        );

        when(menuService.getMenuItems(anyString())).thenReturn(menuItems);

        // Act
        ResponseEntity<List<MenuItemResponse>> response = menuController.getMenuItems(mockUserDetails);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Margherita Pizza");
        assertThat(response.getBody().get(1).getName()).isEqualTo("Pepperoni Pizza");

        verify(menuService).getMenuItems("restaurant@test.com");
    }

    @Test
    @DisplayName("Should get menu items by category successfully")
    void shouldGetMenuItemsByCategorySuccessfully() {
        // Arrange
        List<MenuItemResponse> pizzaItems = Arrays.asList(sampleMenuItemResponse);
        when(menuService.getMenuItemsByCategory(anyString(), eq("Pizza")))
                .thenReturn(pizzaItems);

        // Act
        ResponseEntity<List<MenuItemResponse>> response = menuController.getMenuItemsByCategory(mockUserDetails, "Pizza");

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getCategory()).isEqualTo("Pizza");

        verify(menuService).getMenuItemsByCategory("restaurant@test.com", "Pizza");
    }

    @Test
    @DisplayName("Should update menu item successfully")
    void shouldUpdateMenuItemSuccessfully() {
        // Arrange
        MenuItemRequest updateRequest = new MenuItemRequest(
                "Updated Pizza",
                "Updated description",
                new BigDecimal("15.99"),
                "Pizza",
                true
        );

        MenuItemResponse updatedResponse = new MenuItemResponse(
                1L,
                "Updated Pizza",
                "Updated description",
                new BigDecimal("15.99"),
                "Pizza",
                true,
                1L
        );

        when(menuService.updateMenuItem(anyString(), eq(1L), any(MenuItemRequest.class)))
                .thenReturn(updatedResponse);

        // Act
        ResponseEntity<MenuItemResponse> response = menuController.updateMenuItem(mockUserDetails, 1L, updateRequest);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Pizza");
        assertThat(response.getBody().getPrice()).isEqualTo(new BigDecimal("15.99"));

        verify(menuService).updateMenuItem("restaurant@test.com", 1L, updateRequest);
    }

    @Test
    @DisplayName("Should delete menu item successfully")
    void shouldDeleteMenuItemSuccessfully() {
        // Arrange
        doNothing().when(menuService).deleteMenuItem(anyString(), eq(1L));

        // Act
        ResponseEntity<Map<String, String>> response = menuController.deleteMenuItem(mockUserDetails, 1L);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Menu item deleted successfully");

        verify(menuService).deleteMenuItem("restaurant@test.com", 1L);
    }

    @Test
    @DisplayName("Should toggle availability successfully")
    void shouldToggleAvailabilitySuccessfully() {
        // Arrange
        MenuItemResponse toggledResponse = new MenuItemResponse(
                1L,
                "Margherita Pizza",
                "Classic pizza with fresh mozzarella and basil",
                new BigDecimal("12.99"),
                "Pizza",
                false, // Toggled to unavailable
                1L
        );

        when(menuService.toggleAvailability(anyString(), eq(1L)))
                .thenReturn(toggledResponse);

        // Act
        ResponseEntity<MenuItemResponse> response = menuController.toggleAvailability(mockUserDetails, 1L);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsAvailable()).isFalse();

        verify(menuService).toggleAvailability("restaurant@test.com", 1L);
    }

    @Test
    @DisplayName("Should get public menu successfully")
    void shouldGetPublicMenuSuccessfully() {
        // Arrange
        List<MenuItemResponse> publicMenu = Arrays.asList(sampleMenuItemResponse);
        when(menuService.getMenuItemsByRestaurantId(eq(1L))).thenReturn(publicMenu);

        // Act
        ResponseEntity<List<MenuItemResponse>> response = menuController.getPublicMenu(1L);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Margherita Pizza");

        verify(menuService).getMenuItemsByRestaurantId(1L);
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void shouldHandleServiceExceptionsGracefully() {
        // Arrange
        when(menuService.addMenuItem(anyString(), any(MenuItemRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThatThrownBy(() -> menuController.addMenuItem(mockUserDetails, sampleMenuItemRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Service error");

        verify(menuService).addMenuItem("restaurant@test.com", sampleMenuItemRequest);
    }

    @Test
    @DisplayName("Should extract correct username from UserDetails")
    void shouldExtractCorrectUsernameFromUserDetails() {
        // Arrange
        when(menuService.getMenuItems(eq("restaurant@test.com"))).thenReturn(Arrays.asList(sampleMenuItemResponse));

        // Act
        menuController.getMenuItems(mockUserDetails);

        // Assert
        verify(menuService).getMenuItems("restaurant@test.com");
    }
}