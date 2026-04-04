package com.app.quickbite.auth.service;

import com.app.quickbite.auth.dto.*;
import com.app.quickbite.auth.entity.DeliveryAgent;
import com.app.quickbite.auth.entity.Restaurant;
import com.app.quickbite.auth.entity.User;
import com.app.quickbite.auth.repository.DeliveryAgentRepository;
import com.app.quickbite.auth.repository.RestaurantRepository;
import com.app.quickbite.auth.repository.UserRepository;
import com.app.quickbite.auth.security.JwtUtil;
import com.app.quickbite.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService using Mockito
 * 
 * Tests cover:
 * - Customer registration (success and failure cases)
 * - Restaurant registration (success and failure cases)
 * - Delivery Agent registration (success and failure cases)
 * - User login (success and failure cases)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private DeliveryAgentRepository deliveryAgentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_FULL_NAME = "John Doe";
    private static final String TEST_PHONE = "1234567890";
    private static final String TEST_TOKEN = "jwt.test.token";
    private static final String ENCODED_PASSWORD = "encodedPassword123";

    @Nested
    @DisplayName("Customer Registration Tests")
    class CustomerRegistrationTests {

        private CustomerRegisterRequest customerRequest;

        @BeforeEach
        void setUp() {
            customerRequest = new CustomerRegisterRequest();
            customerRequest.setEmail(TEST_EMAIL);
            customerRequest.setPassword(TEST_PASSWORD);
            customerRequest.setFullName(TEST_FULL_NAME);
            customerRequest.setPhone(TEST_PHONE);
        }

        @Test
        @DisplayName("Should register customer successfully")
        void registerCustomer_Success() {
            // Arrange
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(jwtUtil.generateToken(TEST_EMAIL, "CUSTOMER")).thenReturn(TEST_TOKEN);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            // Act
            AuthResponse response = authService.registerCustomer(customerRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
            assertThat(response.getRole()).isEqualTo("CUSTOMER");
            assertThat(response.getMessage()).contains("successful");

            // Verify interactions
            verify(userRepository).findByEmail(TEST_EMAIL);
            verify(passwordEncoder).encode(TEST_PASSWORD);
            verify(userRepository).save(any(User.class));
            verify(jwtUtil).generateToken(TEST_EMAIL, "CUSTOMER");
        }

        @Test
        @DisplayName("Should capture and verify saved user details")
        void registerCustomer_VerifySavedUserDetails() {
            // Arrange
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(TEST_TOKEN);
            
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // Act
            authService.registerCustomer(customerRequest);

            // Assert - Capture the saved user and verify its properties
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            
            assertThat(savedUser.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(savedUser.getFullName()).isEqualTo(TEST_FULL_NAME);
            assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(savedUser.getPhoneNumber()).isEqualTo(TEST_PHONE);
            assertThat(savedUser.getRole()).isEqualTo("CUSTOMER");
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void registerCustomer_EmailAlreadyExists() {
            // Arrange
            User existingUser = new User();
            existingUser.setEmail(TEST_EMAIL);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.registerCustomer(customerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(TEST_EMAIL);

            // Verify no save or token generation occurred
            verify(userRepository, never()).save(any(User.class));
            verify(jwtUtil, never()).generateToken(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Restaurant Registration Tests")
    class RestaurantRegistrationTests {

        private RestaurantRegisterRequest restaurantRequest;

        @BeforeEach
        void setUp() {
            restaurantRequest = new RestaurantRegisterRequest();
            restaurantRequest.setEmail(TEST_EMAIL);
            restaurantRequest.setPassword(TEST_PASSWORD);
            restaurantRequest.setFullName(TEST_FULL_NAME);
            restaurantRequest.setPhone(TEST_PHONE);
            restaurantRequest.setRestaurantName("Test Restaurant");
            restaurantRequest.setAddress("123 Test Street");
            restaurantRequest.setCuisineType("Italian");
        }

        @Test
        @DisplayName("Should register restaurant successfully")
        void registerRestaurant_Success() {
            // Arrange
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(jwtUtil.generateToken(TEST_EMAIL, "RESTAURANT_OWNER")).thenReturn(TEST_TOKEN);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            // Act
            AuthResponse response = authService.registerRestaurant(restaurantRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
            assertThat(response.getRole()).isEqualTo("RESTAURANT_OWNER");
            assertThat(response.getMessage()).contains("Restaurant registration successful");

            // Verify both user and restaurant are saved
            verify(userRepository).save(any(User.class));
            verify(restaurantRepository).save(any(Restaurant.class));
        }

        @Test
        @DisplayName("Should capture and verify saved restaurant details")
        void registerRestaurant_VerifySavedRestaurantDetails() {
            // Arrange
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(TEST_TOKEN);
            
            ArgumentCaptor<Restaurant> restaurantCaptor = ArgumentCaptor.forClass(Restaurant.class);

            // Act
            authService.registerRestaurant(restaurantRequest);

            // Assert
            verify(restaurantRepository).save(restaurantCaptor.capture());
            Restaurant savedRestaurant = restaurantCaptor.getValue();
            
            assertThat(savedRestaurant.getRestaurantName()).isEqualTo("Test Restaurant");
            assertThat(savedRestaurant.getAddress()).isEqualTo("123 Test Street");
            assertThat(savedRestaurant.getCuisineType()).isEqualTo("Italian");
            assertThat(savedRestaurant.getIsApproved()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when email already exists for restaurant")
        void registerRestaurant_EmailAlreadyExists() {
            // Arrange
            User existingUser = new User();
            existingUser.setEmail(TEST_EMAIL);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.registerRestaurant(restaurantRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(TEST_EMAIL);

            verify(userRepository, never()).save(any(User.class));
            verify(restaurantRepository, never()).save(any(Restaurant.class));
        }
    }

    @Nested
    @DisplayName("Delivery Agent Registration Tests")
    class DeliveryAgentRegistrationTests {

        private AgentRegisterRequest agentRequest;

        @BeforeEach
        void setUp() {
            agentRequest = new AgentRegisterRequest();
            agentRequest.setEmail(TEST_EMAIL);
            agentRequest.setPassword(TEST_PASSWORD);
            agentRequest.setFullName(TEST_FULL_NAME);
            agentRequest.setPhone(TEST_PHONE);
            agentRequest.setVehicleNumber("ABC-1234");
            agentRequest.setAddress("456 Agent Street");
        }

        @Test
        @DisplayName("Should register delivery agent successfully")
        void registerAgent_Success() {
            // Arrange
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(jwtUtil.generateToken(TEST_EMAIL, "DELIVERY_AGENT")).thenReturn(TEST_TOKEN);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            // Act
            AuthResponse response = authService.registerAgent(agentRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
            assertThat(response.getRole()).isEqualTo("DELIVERY_AGENT");
            assertThat(response.getMessage()).contains("Delivery agent registration successful");

            // Verify both user and agent are saved
            verify(userRepository).save(any(User.class));
            verify(deliveryAgentRepository).save(any(DeliveryAgent.class));
        }

        @Test
        @DisplayName("Should capture and verify saved agent details")
        void registerAgent_VerifySavedAgentDetails() {
            // Arrange
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(TEST_TOKEN);
            
            ArgumentCaptor<DeliveryAgent> agentCaptor = ArgumentCaptor.forClass(DeliveryAgent.class);

            // Act
            authService.registerAgent(agentRequest);

            // Assert
            verify(deliveryAgentRepository).save(agentCaptor.capture());
            DeliveryAgent savedAgent = agentCaptor.getValue();
            
            assertThat(savedAgent.getVehicleNumber()).isEqualTo("ABC-1234");
            assertThat(savedAgent.getAddress()).isEqualTo("456 Agent Street");
            assertThat(savedAgent.getIsVerified()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when email already exists for agent")
        void registerAgent_EmailAlreadyExists() {
            // Arrange
            User existingUser = new User();
            existingUser.setEmail(TEST_EMAIL);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.registerAgent(agentRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(TEST_EMAIL);

            verify(userRepository, never()).save(any(User.class));
            verify(deliveryAgentRepository, never()).save(any(DeliveryAgent.class));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        private LoginRequest loginRequest;
        private User existingUser;

        @BeforeEach
        void setUp() {
            loginRequest = new LoginRequest();
            loginRequest.setEmail(TEST_EMAIL);
            loginRequest.setPassword(TEST_PASSWORD);

            existingUser = new User();
            existingUser.setId(1L);
            existingUser.setEmail(TEST_EMAIL);
            existingUser.setPassword(ENCODED_PASSWORD);
            existingUser.setFullName(TEST_FULL_NAME);
            existingUser.setRole("CUSTOMER");
        }

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_Success() {
            // Arrange
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));
            when(jwtUtil.generateToken(TEST_EMAIL, "CUSTOMER")).thenReturn(TEST_TOKEN);

            // Act
            AuthResponse response = authService.login(loginRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
            assertThat(response.getRole()).isEqualTo("CUSTOMER");
            assertThat(response.getMessage()).isEqualTo("Login successful");

            // Verify authentication was attempted
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Should throw exception with invalid credentials")
        void login_InvalidCredentials() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

            // Verify no token generation occurred
            verify(jwtUtil, never()).generateToken(anyString(), anyString());
        }

        @Test
        @DisplayName("Should login with different roles")
        void login_DifferentRoles() {
            // Test with RESTAURANT_OWNER
            existingUser.setRole("RESTAURANT_OWNER");
            
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));
            when(jwtUtil.generateToken(TEST_EMAIL, "RESTAURANT_OWNER")).thenReturn(TEST_TOKEN);

            AuthResponse response = authService.login(loginRequest);

            assertThat(response.getRole()).isEqualTo("RESTAURANT_OWNER");
        }
    }
}
