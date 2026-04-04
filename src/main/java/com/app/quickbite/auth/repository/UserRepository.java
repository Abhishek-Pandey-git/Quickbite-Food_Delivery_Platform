package com.app.quickbite.auth.repository;

import com.app.quickbite.auth.entity.AuthProvider;
import com.app.quickbite.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * USER REPOSITORY - Data Access Layer for User entity
 * 
 * By extending JpaRepository, this interface automatically gets basic CRUD operations:
 * - save(user)
 * - findById(id)
 * - findAll()
 * - deleteById(id)
 * - etc.
 * 
 * We don't need to write any implementation code - Spring Data JPA generates it at runtime!
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by their email address
     * 
     * This method is used during login to look up the user attempting to authenticate.
     * Spring Data JPA automatically generates the SQL query from the method name:
     * "findByEmail" → SELECT * FROM users WHERE email = ?
     * 
     * @param email The email address to search for
     * @return Optional<User> - contains the user if found, empty otherwise
     *         Using Optional prevents NullPointerException - we can check if user exists
     */
    Optional<User> findByEmail(String email);


    
    /**
     * Find a user by OAuth provider and OAuth ID
     * Used to check if user already exists from OAuth login
     * 
     * Example: findByOauthProviderAndOauthId(AuthProvider.GOOGLE, "1234567890")
     * 
     * @param oauthProvider The provider (e.g., AuthProvider.GOOGLE)
     * @param oauthId The OAuth provider's unique ID (e.g., Google's "sub")
     * @return Optional<User> containing the user if found
     */
    Optional<User> findByOauthProviderAndOauthId(AuthProvider oauthProvider, String oauthId);

    /**
     * Find a user by email and OAuth provider
     * Used to detect if email was registered with different auth method
     * 
     * Example: findByEmailAndOauthProvider("user@gmail.com", AuthProvider.GOOGLE)
     * 
     * @param email The user's email address
     * @param authProvider The provider (e.g., AuthProvider.GOOGLE)
     * @return Optional<User> containing the user if found
     */
    Optional<User> findByEmailAndOauthProvider(String email, AuthProvider oauthProvider);

}
