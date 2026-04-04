package com.app.quickbite.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GLOBAL EXCEPTION HANDLER - Centralized Error Handling
 * 
 * @RestControllerAdvice is a special Spring annotation that:
 * 1. Applies to ALL controllers in the application
 * 2. Catches exceptions thrown anywhere in the REST API
 * 3. Returns JSON responses (instead of HTML error pages)
 * 
 * This ensures consistent error response format across the entire application.
 * 
 * WHY THIS IS USEFUL:
 * Without this, if an exception is thrown, Spring returns a default error page
 * which is not ideal for a REST API. We want clean JSON error messages instead.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle UserAlreadyExistsException
     * 
     * This method is automatically called when UserAlreadyExistsException is thrown
     * anywhere in the application.
     * 
     * @param ex The exception that was thrown
     * @return ResponseEntity with error details and HTTP 409 CONFLICT status
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        logger.warn("User already exists: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.CONFLICT, 
            "User Already Exists",
            ex.getMessage()
        );
    }
    
    /**
     * Handle BadCredentialsException
     * 
     * This is thrown by Spring Security when login credentials are invalid
     * (wrong email or wrong password).
     * 
     * @param ex The exception that was thrown
     * @return ResponseEntity with error details and HTTP 401 UNAUTHORIZED status
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.UNAUTHORIZED, 
            "Authentication Failed", 
            "Invalid email or password"
        );
    }
    
    /**
     * Handle IllegalArgumentException
     * 
     * This is thrown when invalid data is provided (e.g., null email, empty password).
     * 
     * @param ex The exception that was thrown
     * @return ResponseEntity with error details and HTTP 400 BAD REQUEST status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.error("Illegal argument exception: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST, 
            "Invalid Request", 
            ex.getMessage()
        );
    }
    
    /**
     * Catch-all handler for any other unexpected exceptions
     * 
     * This is a safety net to ensure we always return JSON, even for errors
     * we didn't explicitly handle above.
     * 
     * @param ex The exception that was thrown
     * @return ResponseEntity with error details and HTTP 500 INTERNAL SERVER ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "Internal Server Error", 
            "An unexpected error occurred: " + ex.getMessage()
        );
    }
    
    /**
     * HELPER METHOD - Build standardized error response
     * 
     * This creates a consistent JSON structure for all errors:
     * {
     *   "timestamp": "2026-04-02T13:37:00",
     *   "status": 409,
     *   "error": "User Already Exists",
     *   "message": "User with email john@example.com already exists"
     * }
     * 
     * @param status HTTP status code (e.g., 409, 401, 400, 500)
     * @param error Short error title
     * @param message Detailed error message
     * @return ResponseEntity containing the error map and status code
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, 
            String error, 
            String message) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        
        return new ResponseEntity<>(errorResponse, status);
    }
}
