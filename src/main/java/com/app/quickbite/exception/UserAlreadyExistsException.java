package com.app.quickbite.exception;

/**
 * CUSTOM EXCEPTION - User Already Exists
 * 
 * This exception is thrown when someone tries to register with an email
 * that's already in the database.
 * 
 * For example:
 * - User tries to register as customer with email "john@example.com"
 * - But "john@example.com" already exists in the users table
 * - We throw this exception to prevent duplicate accounts
 * 
 * Extends RuntimeException so we don't need to declare it in method signatures
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    /**
     * Constructor that accepts a custom error message
     * 
     * @param message The error message (e.g., "User with email john@example.com already exists")
     */
    public UserAlreadyExistsException(String message) {
        super(message); // Pass the message up to RuntimeException
    }
}
