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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AUTH SERVICE - Business Logic for Authentication
 * 
 * This service handles all authentication-related operations:
 * - User registration (customer, restaurant, delivery agent)
 * - User login (authentication and JWT token generation)
 * 
 * @Service: Marks this as a Spring service component (business logic layer)
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private DeliveryAgentRepository deliveryAgentRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder; // For hashing passwords
    
    @Autowired
    private JwtUtil jwtUtil; // For generating JWT tokens
    
    @Autowired
    private AuthenticationManager authenticationManager; // For authenticating login credentials
    
    /**
     * REGISTER CUSTOMER - Create a new customer account
     * 
     * THE FLOW:
     * 1. Check if email already exists (prevent duplicates)
     * 2. Hash the password using BCrypt
     * 3. Create a User entity with role=CUSTOMER
     * 4. Save to database
     * 5. Generate JWT token
     * 6. Return token and success message
     * 
     * @param request CustomerRegisterRequest containing fullName, email, password, phone
     * @return AuthResponse containing JWT token, role, and success message
     * @throws UserAlreadyExistsException if email is already registered
     */
    @Transactional
    public AuthResponse registerCustomer(CustomerRegisterRequest request) {
        logger.info("Starting customer registration for email: {}", request.getEmail());
        
        // Check if user already exists
        logger.debug("Checking if user already exists: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Registration failed - user already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        
        // Create new user entity
        logger.debug("Creating new customer user entity for: {}", request.getEmail());
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash the password!
        user.setPhoneNumber(request.getPhone());
        user.setRole("CUSTOMER");
        
        // Save to database
        logger.debug("Saving customer user to database: {}", request.getEmail());
        userRepository.save(user);
        
        // Generate JWT token
        logger.debug("Generating JWT token for customer: {}", user.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        
        logger.info("Customer registration successful for: {}", user.getEmail());
        // Return response
        return new AuthResponse(token, user.getRole(), "Customer registration successful");
    }
    
    /**
     * REGISTER RESTAURANT - Create a new restaurant owner account + restaurant record
     * 
     * THE FLOW:
     * 1. Check if email already exists
     * 2. Create User entity with role=RESTAURANT_OWNER
     * 3. Save User to database (this is the "hub")
     * 4. Create Restaurant entity linked to the User (this is the "spoke")
     * 5. Save Restaurant to database
     * 6. Generate JWT token
     * 7. Return token and success message
     * 
     * @param request RestaurantRegisterRequest containing user info + restaurant details
     * @return AuthResponse containing JWT token, role, and success message
     * @throws UserAlreadyExistsException if email is already registered
     */
    @Transactional // Ensures both User and Restaurant are saved together (all or nothing)
    public AuthResponse registerRestaurant(RestaurantRegisterRequest request) {
        logger.info("Starting restaurant registration for email: {}", request.getEmail());
        
        // Check if user already exists
        logger.debug("Checking if user already exists: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Registration failed - user already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        
        logger.debug("Creating new restaurant user entity for: {}", request.getEmail());
        // Create and save User (the hub)
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhone());
        user.setRole("RESTAURANT_OWNER");
        
        userRepository.save(user);
        logger.debug("User entity saved with ID: {}", user.getId());
        
        // Create and save Restaurant (the spoke)
        logger.debug("Creating restaurant entity for user: {}", request.getEmail());
        Restaurant restaurant = new Restaurant();
        restaurant.setUser(user); // Link to the user we just created
        restaurant.setRestaurantName(request.getRestaurantName());
        restaurant.setAddress(request.getAddress());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setIsApproved(false); // Default: not approved yet
        
        restaurantRepository.save(restaurant);
        logger.debug("Restaurant entity saved for user: {}", request.getEmail());
        
        // Generate JWT token
        logger.debug("Generating JWT token for user: {}", user.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        
        logger.info("Restaurant registration completed successfully for: {}", request.getEmail());
        // Return response
        return new AuthResponse(token, user.getRole(), "Restaurant registration successful. Pending admin approval.");
    }
    
    /**
     * REGISTER DELIVERY AGENT - Create a new delivery agent account + agent record
     * 
     * THE FLOW:
     * 1. Check if email already exists
     * 2. Create User entity with role=DELIVERY_AGENT
     * 3. Save User to database (the hub)
     * 4. Create DeliveryAgent entity linked to the User (the spoke)
     * 5. Save DeliveryAgent to database
     * 6. Generate JWT token
     * 7. Return token and success message
     * 
     * @param request AgentRegisterRequest containing user info + vehicle number
     * @return AuthResponse containing JWT token, role, and success message
     * @throws UserAlreadyExistsException if email is already registered
     */
    @Transactional
    public AuthResponse registerAgent(AgentRegisterRequest request) {
        logger.info("Starting delivery agent registration for email: {}", request.getEmail());
        
        // Check if user already exists
        logger.debug("Checking if user already exists: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Registration failed - user already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        
        logger.debug("Creating new delivery agent user entity for: {}", request.getEmail());
        // Create and save User (the hub)
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Never log password!
        user.setPhoneNumber(request.getPhone());
        user.setRole("DELIVERY_AGENT");
        
        userRepository.save(user);
        logger.debug("User entity saved with ID: {}", user.getId());
        
        // Create and save DeliveryAgent (the spoke)
        logger.debug("Creating delivery agent entity for user: {}", request.getEmail());
        DeliveryAgent agent = new DeliveryAgent();
        agent.setUser(user); // Link to the user we just created
        agent.setVehicleNumber(request.getVehicleNumber());
        agent.setAddress(request.getAddress());
        agent.setIsVerified(false); // Default: not verified yet
        
        deliveryAgentRepository.save(agent);
        logger.debug("DeliveryAgent entity saved for user: {}", request.getEmail());
        
        // Generate JWT token
        logger.debug("Generating JWT token for user: {}", user.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        
        logger.info("Delivery agent registration completed successfully for: {}", request.getEmail());
        // Return response
        return new AuthResponse(token, user.getRole(), "Delivery agent registration successful. Pending verification.");
    }
    
    /**
     * LOGIN - Authenticate user and generate JWT token
     * 
     * THE FLOW:
     * 1. Use Spring Security's AuthenticationManager to verify credentials
     *    - This internally loads the user from database via UserDetailsService
     *    - Compares the provided password with the stored hash via PasswordEncoder
     *    - If they match, authentication succeeds
     *    - If not, throws BadCredentialsException
     * 
     * 2. If authentication succeeds, load the user from database
     * 3. Generate a JWT token
     * 4. Return token, role, and success message
     * 
     * @param request LoginRequest containing email and password
     * @return AuthResponse containing JWT token, role, and success message
     * @throws BadCredentialsException if email or password is incorrect (handled by GlobalExceptionHandler)
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());
        
        try {
            // STEP 1: Authenticate the user
            // This is THE KEY STEP - Spring Security verifies the credentials
            logger.debug("Authenticating user credentials for: {}", request.getEmail());
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),    // Username (email)
                    request.getPassword()  // Plain text password (will be compared with hash)
                )
            );
            
            // If we reach here, authentication was successful!
            // (If it failed, BadCredentialsException would have been thrown above)
            logger.info("Authentication successful for: {}", request.getEmail());
            
            // STEP 2: Load the full user details from database
            logger.debug("Loading user details from database: {}", request.getEmail());
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found")); // This should never happen since we just authenticated
            
            // STEP 3: Generate JWT token
            logger.debug("Generating JWT token for: {}", user.getEmail());
            String token = jwtUtil.generateToken(user.getEmail(),user.getRole());
            
            logger.info("Login successful for user: {} with role: {}", user.getEmail(), user.getRole());
            // STEP 4: Return response
            return new AuthResponse(token, user.getRole(), "Login successful");
        } catch (Exception e) {
            logger.error("Login failed for email: {} - {}", request.getEmail(), e.getMessage());
            throw e; // Re-throw to be handled by GlobalExceptionHandler
        }
    }
}
