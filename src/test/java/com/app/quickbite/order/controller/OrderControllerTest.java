package com.app.quickbite.order.controller;

import com.app.quickbite.auth.repository.RestaurantRepository;
import com.app.quickbite.auth.repository.UserRepository;
import com.app.quickbite.order.dto.OrderItemResponse;
import com.app.quickbite.order.dto.OrderResponse;
import com.app.quickbite.order.dto.OrderStatusUpdateRequest;
import com.app.quickbite.order.dto.PlaceOrderRequest;
import com.app.quickbite.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for OrderController endpoints.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Controller Tests")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private OrderController orderController;

    private UserDetails mockUserDetails;
        private com.app.quickbite.auth.entity.User mockDomainUser;
    private PlaceOrderRequest placeOrderRequest;
    private OrderStatusUpdateRequest statusUpdateRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        mockUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("customer@test.com")
                .password("password")
                .authorities("ROLE_CUSTOMER")
                .build();

        mockDomainUser = new com.app.quickbite.auth.entity.User();
        mockDomainUser.setId(10L);
        mockDomainUser.setFullName("Test Customer");
        mockDomainUser.setEmail("customer@test.com");
        mockDomainUser.setPassword("encoded-password");
        mockDomainUser.setPhoneNumber("9876543210");
        mockDomainUser.setRole("CUSTOMER");

        placeOrderRequest = new PlaceOrderRequest(
                1L,
                List.of(),
                "123 Delivery Street, Bangalore"
        );

        statusUpdateRequest = new OrderStatusUpdateRequest("CONFIRMED");

        orderResponse = new OrderResponse(
                100L,
                "Pizza Palace",
                "PLACED",
                new BigDecimal("499.00"),
                "123 Delivery Street, Bangalore",
                LocalDateTime.of(2026, 4, 6, 10, 30),
                List.of(
                        new OrderItemResponse(
                                11L,
                                "Margherita Pizza",
                                2,
                                new BigDecimal("249.50"),
                                new BigDecimal("499.00")
                        )
                )
        );
    }

    /**
     * Should place a new order successfully for the authenticated customer.
     */
    @Test
    @DisplayName("Should place order successfully")
    void shouldPlaceOrderSuccessfully() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(mockDomainUser));
        when(orderService.placeOrder(eq(10L), any(PlaceOrderRequest.class))).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response = orderController.placeOrder(placeOrderRequest, mockUserDetails);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderId()).isEqualTo(100L);
        assertThat(response.getBody().getRestaurantName()).isEqualTo("Pizza Palace");
        assertThat(response.getBody().getStatus()).isEqualTo("PLACED");
        assertThat(response.getBody().getItems()).hasSize(1);

        verify(userRepository).findByEmail("customer@test.com");
        verify(orderService).placeOrder(10L, placeOrderRequest);
    }

    /**
     * Should return the current customer's order history.
     */
    @Test
    @DisplayName("Should get customer orders successfully")
    void shouldGetCustomerOrdersSuccessfully() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(mockDomainUser));
        when(orderService.getCustomerOrders(10L)).thenReturn(List.of(orderResponse));

        ResponseEntity<List<OrderResponse>> response = orderController.getCustomerOrders(mockUserDetails);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getOrderId()).isEqualTo(100L);

        verify(userRepository).findByEmail("customer@test.com");
        verify(orderService).getCustomerOrders(10L);
    }

    /**
     * Should update an order status successfully.
     */
    @Test
    @DisplayName("Should update order status successfully")
    void shouldUpdateOrderStatusSuccessfully() {
        OrderResponse updatedResponse = new OrderResponse(
                100L,
                "Pizza Palace",
                "CONFIRMED",
                new BigDecimal("499.00"),
                "123 Delivery Street, Bangalore",
                LocalDateTime.of(2026, 4, 6, 10, 30),
                List.of()
        );

        when(orderService.updateOrderStatus(100L, "CONFIRMED")).thenReturn(updatedResponse);

        ResponseEntity<OrderResponse> response = orderController.updateOrderStatus(100L, statusUpdateRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("CONFIRMED");

        verify(orderService).updateOrderStatus(100L, "CONFIRMED");
    }

    /**
     * Should return incoming orders for the restaurant owner.
     */
    @Test
    @DisplayName("Should get restaurant orders successfully")
    void shouldGetRestaurantOrdersSuccessfully() {
        com.app.quickbite.auth.entity.Restaurant restaurant = new com.app.quickbite.auth.entity.Restaurant();
        restaurant.setId(77L);
        restaurant.setRestaurantName("Pizza Palace");

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(mockDomainUser));
        when(restaurantRepository.findByUserId(10L)).thenReturn(Optional.of(restaurant));
        when(orderService.getRestaurantOrders(77L, "PLACED")).thenReturn(List.of(orderResponse));

        ResponseEntity<List<OrderResponse>> response = orderController.getRestaurantOrders(mockUserDetails, "PLACED");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getOrderId()).isEqualTo(100L);

        verify(userRepository).findByEmail("customer@test.com");
        verify(restaurantRepository).findByUserId(10L);
        verify(orderService).getRestaurantOrders(77L, "PLACED");
    }

    /**
     * Should propagate validation and service exceptions for checkout failures.
     */
    @Test
    @DisplayName("Should handle order placement exceptions")
    void shouldHandleOrderPlacementExceptions() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(mockDomainUser));
        when(orderService.placeOrder(eq(10L), any(PlaceOrderRequest.class)))
                .thenThrow(new RuntimeException("Menu item is out of stock"));

        assertThatThrownBy(() -> orderController.placeOrder(placeOrderRequest, mockUserDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Menu item is out of stock");

        verify(userRepository).findByEmail("customer@test.com");
        verify(orderService).placeOrder(10L, placeOrderRequest);
    }

    /**
     * Should fail fast when the authenticated user cannot be resolved.
     */
    @Test
    @DisplayName("Should reject missing authenticated user")
    void shouldRejectMissingAuthenticatedUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderController.getCustomerOrders(mockUserDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Authenticated user not found");

        verify(userRepository).findByEmail("customer@test.com");
        verifyNoInteractions(orderService);
    }
}