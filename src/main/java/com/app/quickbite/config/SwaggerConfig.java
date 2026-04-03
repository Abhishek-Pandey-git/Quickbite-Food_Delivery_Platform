package com.app.quickbite.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SWAGGER CONFIGURATION - API Documentation Setup
 * 
 * This configuration sets up Swagger/OpenAPI documentation for the QuickBite API.
 * It provides:
 * 1. Interactive API documentation at /swagger-ui.html
 * 2. JSON schema at /v3/api-docs
 * 3. JWT Bearer token authentication support
 * 4. Comprehensive API metadata
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configure OpenAPI documentation
     * 
     * @return OpenAPI Configured OpenAPI specification
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(getApiInfo())
                .servers(getServers())
                .addSecurityItem(getSecurityRequirement())
                .components(getComponents());
    }
    
    /**
     * API Information and Metadata
     */
    private Info getApiInfo() {
        return new Info()
                .title("QuickBite Food Delivery API")
                .description("""
                    **QuickBite Food Delivery Platform REST API**
                    
                    A comprehensive food delivery application with multi-role authentication system.
                    
                    ## Features:
                    - 🔐 **JWT Authentication** - Secure token-based authentication
                    - 👥 **Multi-Role System** - Customer, Restaurant Owner, Delivery Agent
                    - 🏗️ **Hub & Spoke Architecture** - Centralized user management
                    - 🔒 **Role-Based Access Control** - Endpoint protection based on user roles
                    
                    ## Authentication:
                    1. Register or login to get a JWT token
                    2. Include token in Authorization header: `Bearer <your-token>`
                    3. Access protected endpoints based on your role
                    
                    ## User Roles:
                    - **CUSTOMER**: Browse and order food
                    - **RESTAURANT_OWNER**: Manage restaurant and orders  
                    - **DELIVERY_AGENT**: Handle delivery assignments
                    """)
                .version("1.0.0")
                .contact(getContactInfo());
    }
    
    /**
     * API Contact Information
     */
    private Contact getContactInfo() {
        return new Contact()
                .name("QuickBite Development Team")
                .email("support@quickbite.com")
                .url("https://quickbite.com");
    }
    
    /**
     * API Servers Configuration
     */
    private List<Server> getServers() {
        return List.of(
            new Server()
                .url("http://localhost:8080")
                .description("Development Server"),
            new Server()
                .url("https://api.quickbite.com")
                .description("Production Server (Coming Soon)")
        );
    }
    
    /**
     * Security Requirements - JWT Bearer Token
     */
    private SecurityRequirement getSecurityRequirement() {
        return new SecurityRequirement().addList("Bearer Authentication");
    }
    
    /**
     * Security Components - JWT Bearer Token Configuration
     */
    private Components getComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token obtained from login endpoint")
                );
    }
}