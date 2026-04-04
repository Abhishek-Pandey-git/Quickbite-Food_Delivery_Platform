package com.app.quickbite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
/**
 * QUICKBITE APPLICATION - Main Entry Point
 * 
 * This is the main class that starts the Spring Boot application.
 * 
 * @SpringBootApplication: A convenience annotation that combines:
 *   - @Configuration: Marks this as a configuration class
 *   - @EnableAutoConfiguration: Enables Spring Boot's auto-configuration
 *   - @ComponentScan: Scans for Spring components (controllers, services, etc.)
 * 
 * IMPORTANT ANNOTATIONS FOR MULTI-PACKAGE STRUCTURE:
 * Since our entities, repositories, and other components are in com.quickbite.*
 * but our main class is in com.app.quickbite, we need to explicitly tell Spring
 * where to find our components.
 */
@SpringBootApplication(scanBasePackages = {
    "com.app.quickbite"   // Main package
   
})
@EnableAsync 
public class QuickbiteApplication {
    
    /**
     * Main method - the entry point of the application
     * 
     * This method:
     * 1. Starts the Spring Boot application
     * 2. Sets up the embedded Tomcat server (on port 8080 by default)
     * 3. Initializes the H2 database
     * 4. Loads all Spring beans (controllers, services, repositories, etc.)
     * 5. Configures Spring Security
     * 
     * @param args Command line arguments (not used in this MVP)
     */
    public static void main(String[] args) {
        SpringApplication.run(QuickbiteApplication.class, args);
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          QuickBite Food Delivery Platform                ║");
        System.out.println("║          Started Successfully!                            ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  Server:    http://localhost:8080                         ║");
        System.out.println("║  H2 Console: http://localhost:8080/h2-console             ║");
        System.out.println("║  JDBC URL:   jdbc:h2:mem:quickbitedb                      ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  API Endpoints:                                           ║");
        System.out.println("║  POST /api/auth/register/customer                         ║");
        System.out.println("║  POST /api/auth/register/restaurant                       ║");
        System.out.println("║  POST /api/auth/register/agent                            ║");
        System.out.println("║  POST /api/auth/login                                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        
        return new BCryptPasswordEncoder();
    }
}
