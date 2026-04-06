
package com.app.quickbite.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.app.quickbite.auth.security.CustomUserDetailsService;
import com.app.quickbite.auth.security.JwtAuthenticationFilter;

/**
 * SECURITY CONFIGURATION - The Heart of Spring Security Setup
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
   
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final PasswordEncoder passwordEncoder;
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, 
                         CustomUserDetailsService customUserDetailsService,
                         OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
                         PasswordEncoder passwordEncoder) {
        logger.info("Initializing SecurityConfig with JWT filter, UserDetailsService, and OAuth2 handler");
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain...");
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/otp/**").permitAll()
                .requestMatchers("/api/restaurants/**").permitAll()  // Public: Browse restaurants
                .requestMatchers("/api/restaurant/*/menu").permitAll()  // Public: View restaurant menus
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/oauth2/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/api/restaurant/**").hasRole("RESTAURANT_OWNER")  // Protected: Restaurant management
                .requestMatchers("/api/orders/restaurant").hasRole("RESTAURANT_OWNER")  // Protected: Restaurant incoming orders
                .anyRequest().authenticated()
            )
            
            // SESSION: IF_REQUIRED for OAuth2 flow (needs session for state/PKCE)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            
            // OAUTH2 LOGIN: Let Spring handle the entire flow
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler)
                .failureUrl("http://localhost:5173/?error=oauth2_failed")
            )
            
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        logger.info("SecurityFilterChain configured successfully");
        return http.build();
    }
    
    /**
     * PASSWORD ENCODER - BCrypt for hashing passwords
     * 
     * BCrypt is a one-way hashing algorithm (you can't decrypt it back to plain text).
     * It automatically adds a random "salt" to prevent rainbow table attacks.
     * 
     * HOW IT WORKS:
     * - Registration: User provides password "myPassword123"
     *   → We hash it: "$2a$10$AbCdEf..." (60 characters)
     *   → Store the hash in database
     * 
     * - Login: User provides password "myPassword123"
     *   → We hash it and compare with stored hash
     *   → If they match, password is correct
     * 
     * @return PasswordEncoder BCrypt password encoder
     */
    
    
    /**
     * AUTHENTICATION PROVIDER - Combines UserDetailsService and PasswordEncoder
     * 
     * This provider is responsible for:
     * 1. Loading user from database (via UserDetailsService)
     * 2. Comparing passwords (via PasswordEncoder)
     * 
     * It's the bridge between Spring Security and our custom user loading logic.
     * 
     * @return AuthenticationProvider Configured DAO authentication provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        logger.info("Creating DaoAuthenticationProvider bean");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        logger.debug("DaoAuthenticationProvider configured with CustomUserDetailsService and BCryptPasswordEncoder");
        return provider;
    }
    
    /**
     * AUTHENTICATION MANAGER - Manages the authentication process
     * 
     * This is used by our AuthService during login.
     * When we call authenticationManager.authenticate(...), it:
     * 1. Uses the AuthenticationProvider we defined above
     * 2. Loads the user via UserDetailsService
     * 3. Verifies the password via PasswordEncoder
     * 4. Returns an Authentication object if successful
     * 5. Throws BadCredentialsException if credentials are invalid
     * 
     * @param config Authentication configuration
     * @return AuthenticationManager The authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        logger.info("Creating AuthenticationManager bean");
        return config.getAuthenticationManager();
    }
    
    /**
     * CORS CONFIGURATION - Allow React frontend to make cross-origin requests
     * 
     * CORS (Cross-Origin Resource Sharing) allows web pages from one domain
     * to make AJAX requests to another domain. Without this, browsers block
     * requests from localhost:5173 (React) to localhost:8080 (Spring Boot).
     * 
     * This configuration:
     * 1. Allows requests from React development servers (ports 5173-5180)
     * 2. Allows all HTTP methods (GET, POST, PUT, DELETE, OPTIONS)
     * 3. Allows all headers including Authorization for JWT tokens
     * 4. Allows credentials (cookies, authorization headers)
     * 5. Caches preflight requests for 1 hour (3600 seconds)
     * 
     * @return CorsConfigurationSource The CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("Configuring CORS for React frontend");
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow React development servers (Vite typically uses ports 5173-5180)
        configuration.addAllowedOrigin("http://localhost:3000");  // Create React App default
        configuration.addAllowedOrigin("http://localhost:5173");  // Vite default
        configuration.addAllowedOrigin("http://localhost:5174");  // Vite alternative
        configuration.addAllowedOrigin("http://localhost:5175");  // Vite alternative
        configuration.addAllowedOrigin("http://localhost:5176");  // Vite alternative
        configuration.addAllowedOrigin("http://localhost:5177");  // Vite alternative
        configuration.addAllowedOrigin("http://localhost:5178");  // Vite alternative
        configuration.addAllowedOrigin("http://localhost:5179");  // Vite alternative
        configuration.addAllowedOrigin("http://localhost:5180");  // Vite alternative
        
        // Allow all HTTP methods
        configuration.addAllowedMethod("*");
        
        // Allow all headers (including Authorization for JWT tokens)
        configuration.addAllowedHeader("*");
        
        // Allow credentials (needed for cookies and authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply CORS configuration to all endpoints
        source.registerCorsConfiguration("/**", configuration);
        
        logger.debug("CORS configured for React frontend on multiple ports");
        return source;
    }
}
