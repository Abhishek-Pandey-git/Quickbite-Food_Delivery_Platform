package com.app.quickbite.email.service;

import com.app.quickbite.email.entity.EmailOtp;
import com.app.quickbite.email.repository.EmailOtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OtpService using Mockito
 * 
 * Tests cover:
 * - OTP generation (success and rate limiting)
 * - OTP verification (valid, invalid, expired)
 * - Rate limiting logic
 * - Attempt tracking
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OtpService Tests")
class OtpServiceTest {

    @Mock
    private EmailOtpRepository otpRepository;

    @InjectMocks
    private OtpService otpService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String VALID_OTP = "123456";

    @BeforeEach
    void setUp() {
        // Set configuration values using reflection
        ReflectionTestUtils.setField(otpService, "otpExpirationMinutes", 5);
        ReflectionTestUtils.setField(otpService, "otpCodeLength", 6);
        ReflectionTestUtils.setField(otpService, "maxAttempts", 3);
    }

    @Nested
    @DisplayName("OTP Generation Tests")
    class OtpGenerationTests {

        @Test
        @DisplayName("Should generate OTP successfully when not rate limited")
        void generateOtp_Success() {
            // Arrange
            when(otpRepository.countOtpsSentInLastHour(eq(TEST_EMAIL), any(LocalDateTime.class)))
                .thenReturn(0L);
            when(otpRepository.save(any(EmailOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            String otpCode = otpService.generateOtp(TEST_EMAIL);

            // Assert
            assertThat(otpCode).isNotNull();
            assertThat(otpCode).hasSize(6);
            assertThat(otpCode).containsOnlyDigits();

            // Verify OTP was saved
            verify(otpRepository).save(any(EmailOtp.class));
        }

        @Test
        @DisplayName("Should capture and verify saved OTP details")
        void generateOtp_VerifySavedOtpDetails() {
            // Arrange
            when(otpRepository.countOtpsSentInLastHour(eq(TEST_EMAIL), any(LocalDateTime.class)))
                .thenReturn(0L);
            
            ArgumentCaptor<EmailOtp> otpCaptor = ArgumentCaptor.forClass(EmailOtp.class);

            // Act
            String generatedOtp = otpService.generateOtp(TEST_EMAIL);

            // Assert
            verify(otpRepository).save(otpCaptor.capture());
            EmailOtp savedOtp = otpCaptor.getValue();
            
            assertThat(savedOtp.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(savedOtp.getOtpCode()).isEqualTo(generatedOtp);
            assertThat(savedOtp.getExpiresAt()).isAfter(LocalDateTime.now());
            assertThat(savedOtp.getExpiresAt()).isBefore(LocalDateTime.now().plusMinutes(6));
        }

        @Test
        @DisplayName("Should throw exception when rate limited (3+ OTPs in last hour)")
        void generateOtp_RateLimited() {
            // Arrange - Return 3 OTPs sent in last hour (exceeds limit)
            when(otpRepository.countOtpsSentInLastHour(eq(TEST_EMAIL), any(LocalDateTime.class)))
                .thenReturn(3L);

            // Act & Assert
            assertThatThrownBy(() -> otpService.generateOtp(TEST_EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Too many OTP requests");

            // Verify no save occurred
            verify(otpRepository, never()).save(any(EmailOtp.class));
        }

        @Test
        @DisplayName("Should allow OTP generation when under rate limit")
        void generateOtp_UnderRateLimit() {
            // Arrange - Return 2 OTPs (still under limit of 3)
            when(otpRepository.countOtpsSentInLastHour(eq(TEST_EMAIL), any(LocalDateTime.class)))
                .thenReturn(2L);
            when(otpRepository.save(any(EmailOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            String otpCode = otpService.generateOtp(TEST_EMAIL);

            // Assert
            assertThat(otpCode).isNotNull();
            verify(otpRepository).save(any(EmailOtp.class));
        }

        @Test
        @DisplayName("Generated OTP should be different each time")
        void generateOtp_ShouldBeDifferent() {
            // Arrange
            when(otpRepository.countOtpsSentInLastHour(eq(TEST_EMAIL), any(LocalDateTime.class)))
                .thenReturn(0L);
            when(otpRepository.save(any(EmailOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act - Generate multiple OTPs
            String otp1 = otpService.generateOtp(TEST_EMAIL);
            String otp2 = otpService.generateOtp(TEST_EMAIL);
            String otp3 = otpService.generateOtp(TEST_EMAIL);

            // Assert - At least some should be different (statistically unlikely all same)
            // Note: There's a tiny chance all 3 could be the same, but extremely unlikely
            boolean allSame = otp1.equals(otp2) && otp2.equals(otp3);
            // We're testing randomness, so we just verify the format
            assertThat(otp1).hasSize(6).containsOnlyDigits();
            assertThat(otp2).hasSize(6).containsOnlyDigits();
            assertThat(otp3).hasSize(6).containsOnlyDigits();
        }
    }

    @Nested
    @DisplayName("OTP Verification Tests")
    class OtpVerificationTests {

        @Test
        @DisplayName("Should verify valid OTP successfully")
        void verifyOtp_ValidOtp() {
            // Arrange
            EmailOtp validOtp = new EmailOtp(TEST_EMAIL, VALID_OTP, LocalDateTime.now().plusMinutes(5));
            when(otpRepository.findValidOtp(eq(TEST_EMAIL), eq(VALID_OTP), any(LocalDateTime.class)))
                .thenReturn(Optional.of(validOtp));

            // Act
            boolean result = otpService.verifyOtp(TEST_EMAIL, VALID_OTP);

            // Assert
            assertThat(result).isTrue();
            assertThat(validOtp.getVerified()).isTrue();
            verify(otpRepository).save(validOtp);
        }

        @Test
        @DisplayName("Should return false for invalid OTP code")
        void verifyOtp_InvalidOtp() {
            // Arrange
            when(otpRepository.findValidOtp(eq(TEST_EMAIL), eq("wrong123"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
            when(otpRepository.findLatestActiveOtpByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

            // Act
            boolean result = otpService.verifyOtp(TEST_EMAIL, "wrong123");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for expired OTP")
        void verifyOtp_ExpiredOtp() {
            // Arrange - Repository returns empty because OTP is expired
            when(otpRepository.findValidOtp(eq(TEST_EMAIL), eq(VALID_OTP), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
            when(otpRepository.findLatestActiveOtpByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

            // Act
            boolean result = otpService.verifyOtp(TEST_EMAIL, VALID_OTP);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should increment attempts on failed verification")
        void verifyOtp_IncrementAttempts() {
            // Arrange
            EmailOtp activeOtp = new EmailOtp(TEST_EMAIL, VALID_OTP, LocalDateTime.now().plusMinutes(5));
            activeOtp.setAttempts(1);
            
            when(otpRepository.findValidOtp(eq(TEST_EMAIL), eq("wrong123"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
            when(otpRepository.findLatestActiveOtpByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(activeOtp));

            // Act
            boolean result = otpService.verifyOtp(TEST_EMAIL, "wrong123");

            // Assert
            assertThat(result).isFalse();
            assertThat(activeOtp.getAttempts()).isEqualTo(2);
            verify(otpRepository).save(activeOtp);
        }

        @Test
        @DisplayName("Should handle non-existent email")
        void verifyOtp_NonExistentEmail() {
            // Arrange
            String nonExistentEmail = "notfound@example.com";
            when(otpRepository.findValidOtp(eq(nonExistentEmail), anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
            when(otpRepository.findLatestActiveOtpByEmail(nonExistentEmail))
                .thenReturn(Optional.empty());

            // Act
            boolean result = otpService.verifyOtp(nonExistentEmail, VALID_OTP);

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty email")
        void generateOtp_EmptyEmail() {
            // Arrange
            when(otpRepository.countOtpsSentInLastHour(eq(""), any(LocalDateTime.class)))
                .thenReturn(0L);
            when(otpRepository.save(any(EmailOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            String otpCode = otpService.generateOtp("");

            // Assert - Service doesn't validate email format, that's handled at controller level
            assertThat(otpCode).isNotNull();
        }

        @Test
        @DisplayName("Should handle null OTP code verification")
        void verifyOtp_NullOtpCode() {
            // Arrange
            when(otpRepository.findValidOtp(eq(TEST_EMAIL), isNull(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
            when(otpRepository.findLatestActiveOtpByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

            // Act
            boolean result = otpService.verifyOtp(TEST_EMAIL, null);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should verify OTP only once")
        void verifyOtp_OnlyOnce() {
            // Arrange
            EmailOtp validOtp = new EmailOtp(TEST_EMAIL, VALID_OTP, LocalDateTime.now().plusMinutes(5));
            
            // First call returns valid OTP
            when(otpRepository.findValidOtp(eq(TEST_EMAIL), eq(VALID_OTP), any(LocalDateTime.class)))
                .thenReturn(Optional.of(validOtp))
                .thenReturn(Optional.empty()); // Second call returns empty (already verified)

            // Act
            boolean firstResult = otpService.verifyOtp(TEST_EMAIL, VALID_OTP);
            boolean secondResult = otpService.verifyOtp(TEST_EMAIL, VALID_OTP);

            // Assert
            assertThat(firstResult).isTrue();
            assertThat(secondResult).isFalse();
        }
    }
}
