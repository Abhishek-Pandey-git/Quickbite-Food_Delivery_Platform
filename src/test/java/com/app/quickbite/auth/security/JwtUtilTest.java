package com.app.quickbite.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtUtil
 * 
 * Tests cover:
 * - Token generation
 * - Token validation
 * - Username extraction
 * - Role extraction
 * - Token expiration
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_ROLE = "CUSTOMER";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid JWT token")
        void generateToken_Success() {
            // Act
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);

            // Assert
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            // JWT tokens have 3 parts separated by dots
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void generateToken_DifferentUsersGetDifferentTokens() {
            // Act
            String token1 = jwtUtil.generateToken("user1@example.com", "CUSTOMER");
            String token2 = jwtUtil.generateToken("user2@example.com", "CUSTOMER");

            // Assert
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Should generate different tokens for different roles")
        void generateToken_DifferentRolesGetDifferentTokens() {
            // Act
            String customerToken = jwtUtil.generateToken(TEST_EMAIL, "CUSTOMER");
            String restaurantToken = jwtUtil.generateToken(TEST_EMAIL, "RESTAURANT_OWNER");

            // Assert
            assertThat(customerToken).isNotEqualTo(restaurantToken);
        }

        @Test
        @DisplayName("Should generate tokens with correct structure (3 parts)")
        void generateToken_CorrectStructure() {
            // Act
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);

            // Assert - JWT tokens have 3 parts: header.payload.signature
            String[] parts = token.split("\\.");
            assertThat(parts).hasSize(3);
            // Each part should be non-empty
            assertThat(parts[0]).isNotEmpty(); // Header
            assertThat(parts[1]).isNotEmpty(); // Payload
            assertThat(parts[2]).isNotEmpty(); // Signature
        }
    }

    @Nested
    @DisplayName("Username Extraction Tests")
    class UsernameExtractionTests {

        @Test
        @DisplayName("Should extract username from token")
        void extractUsername_Success() {
            // Arrange
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);

            // Act
            String extractedUsername = jwtUtil.extractUsername(token);

            // Assert
            assertThat(extractedUsername).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("Should extract correct username for different users")
        void extractUsername_DifferentUsers() {
            // Arrange
            String email1 = "user1@example.com";
            String email2 = "admin@example.com";
            String token1 = jwtUtil.generateToken(email1, "CUSTOMER");
            String token2 = jwtUtil.generateToken(email2, "ADMIN");

            // Act
            String extracted1 = jwtUtil.extractUsername(token1);
            String extracted2 = jwtUtil.extractUsername(token2);

            // Assert
            assertThat(extracted1).isEqualTo(email1);
            assertThat(extracted2).isEqualTo(email2);
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void extractUsername_InvalidToken() {
            // Arrange
            String invalidToken = "invalid.token.here";

            // Act & Assert
            assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should throw exception for malformed token")
        void extractUsername_MalformedToken() {
            // Arrange
            String malformedToken = "not-a-jwt";

            // Act & Assert
            assertThatThrownBy(() -> jwtUtil.extractUsername(malformedToken))
                .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Role Extraction Tests")
    class RoleExtractionTests {

        @Test
        @DisplayName("Should extract role from token")
        void extractRole_Success() {
            // Arrange
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);

            // Act
            String extractedRole = jwtUtil.extractRole(token);

            // Assert
            assertThat(extractedRole).isEqualTo(TEST_ROLE);
        }

        @Test
        @DisplayName("Should extract correct role for different user types")
        void extractRole_DifferentRoles() {
            // Arrange
            String customerToken = jwtUtil.generateToken(TEST_EMAIL, "CUSTOMER");
            String restaurantToken = jwtUtil.generateToken(TEST_EMAIL, "RESTAURANT_OWNER");
            String agentToken = jwtUtil.generateToken(TEST_EMAIL, "DELIVERY_AGENT");

            // Act & Assert
            assertThat(jwtUtil.extractRole(customerToken)).isEqualTo("CUSTOMER");
            assertThat(jwtUtil.extractRole(restaurantToken)).isEqualTo("RESTAURANT_OWNER");
            assertThat(jwtUtil.extractRole(agentToken)).isEqualTo("DELIVERY_AGENT");
        }

        @Test
        @DisplayName("Should throw exception for invalid token when extracting role")
        void extractRole_InvalidToken() {
            // Arrange
            String invalidToken = "invalid.token.here";

            // Act & Assert
            assertThatThrownBy(() -> jwtUtil.extractRole(invalidToken))
                .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate correct token")
        void validateToken_ValidToken() {
            // Arrange
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);

            // Act
            boolean isValid = jwtUtil.validateToken(token, TEST_EMAIL);

            // Assert
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject token with wrong username")
        void validateToken_WrongUsername() {
            // Arrange
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);

            // Act
            boolean isValid = jwtUtil.validateToken(token, "wrong@example.com");

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject invalid token")
        void validateToken_InvalidToken() {
            // Arrange
            String invalidToken = "invalid.token.signature";

            // Act
            boolean isValid = jwtUtil.validateToken(invalidToken, TEST_EMAIL);

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject tampered token")
        void validateToken_TamperedToken() {
            // Arrange
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);
            // Tamper with the signature part
            String[] parts = token.split("\\.");
            String tamperedToken = parts[0] + "." + parts[1] + ".tamperedsignature";

            // Act
            boolean isValid = jwtUtil.validateToken(tamperedToken, TEST_EMAIL);

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject empty token")
        void validateToken_EmptyToken() {
            // Act
            boolean isValid = jwtUtil.validateToken("", TEST_EMAIL);

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should handle null username gracefully")
        void validateToken_NullUsername() {
            // Arrange
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);

            // Act
            boolean isValid = jwtUtil.validateToken(token, null);

            // Assert
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle email with special characters")
        void generateToken_EmailWithSpecialChars() {
            // Arrange
            String specialEmail = "test+special@sub.example.com";

            // Act
            String token = jwtUtil.generateToken(specialEmail, TEST_ROLE);
            String extractedEmail = jwtUtil.extractUsername(token);

            // Assert
            assertThat(extractedEmail).isEqualTo(specialEmail);
        }

        @Test
        @DisplayName("Should handle long email addresses")
        void generateToken_LongEmail() {
            // Arrange
            String longEmail = "verylongemailaddressthatisunusuallylong@verylongdomainname.example.com";

            // Act
            String token = jwtUtil.generateToken(longEmail, TEST_ROLE);
            String extractedEmail = jwtUtil.extractUsername(token);

            // Assert
            assertThat(extractedEmail).isEqualTo(longEmail);
        }

        @Test
        @DisplayName("Should handle custom roles")
        void generateToken_CustomRole() {
            // Arrange
            String customRole = "SUPER_ADMIN";

            // Act
            String token = jwtUtil.generateToken(TEST_EMAIL, customRole);
            String extractedRole = jwtUtil.extractRole(token);

            // Assert
            assertThat(extractedRole).isEqualTo(customRole);
        }

        @Test
        @DisplayName("Token should be consistent across extractions")
        void multipleExtractions_ConsistentResults() {
            // Arrange
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);

            // Act - Extract multiple times
            String username1 = jwtUtil.extractUsername(token);
            String username2 = jwtUtil.extractUsername(token);
            String role1 = jwtUtil.extractRole(token);
            String role2 = jwtUtil.extractRole(token);

            // Assert
            assertThat(username1).isEqualTo(username2);
            assertThat(role1).isEqualTo(role2);
        }
    }
}
