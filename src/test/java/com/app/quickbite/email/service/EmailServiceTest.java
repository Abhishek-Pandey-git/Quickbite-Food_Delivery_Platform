package com.app.quickbite.email.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailService using Mockito
 * 
 * Tests cover:
 * - OTP email sending (success and failure cases)
 * - Email content formatting
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_OTP = "123456";
    private static final String TEST_USERNAME = "John Doe";
    private static final String FROM_ADDRESS = "noreply@quickbite.com";
    private static final String FROM_NAME = "QuickBite Support";

    @BeforeEach
    void setUp() {
        // Set configuration values using reflection
        ReflectionTestUtils.setField(emailService, "fromAddress", FROM_ADDRESS);
        ReflectionTestUtils.setField(emailService, "fromName", FROM_NAME);
    }

    @Nested
    @DisplayName("Send OTP Email Tests")
    class SendOtpEmailTests {

        @Test
        @DisplayName("Should send OTP email successfully")
        void sendOtpEmail_Success() {
            // Arrange
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act - Should not throw exception
            assertThatCode(() -> emailService.sendOtpEmail(TEST_EMAIL, TEST_OTP, TEST_USERNAME))
                .doesNotThrowAnyException();

            // Verify email was sent
            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should capture and verify email content")
        void sendOtpEmail_VerifyEmailContent() {
            // Arrange
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act
            emailService.sendOtpEmail(TEST_EMAIL, TEST_OTP, TEST_USERNAME);

            // Assert
            verify(mailSender).send(messageCaptor.capture());
            SimpleMailMessage sentMessage = messageCaptor.getValue();

            assertThat(sentMessage.getTo()).containsExactly(TEST_EMAIL);
            assertThat(sentMessage.getFrom()).isEqualTo(FROM_ADDRESS);
            assertThat(sentMessage.getSubject()).contains("QuickBite");
            assertThat(sentMessage.getSubject()).contains("Verification");
            assertThat(sentMessage.getText()).contains(TEST_USERNAME);
            assertThat(sentMessage.getText()).contains(TEST_OTP);
            assertThat(sentMessage.getText()).contains("5 minutes");
        }

        @Test
        @DisplayName("Should include OTP code prominently in email")
        void sendOtpEmail_ContainsOtpCode() {
            // Arrange
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act
            emailService.sendOtpEmail(TEST_EMAIL, TEST_OTP, TEST_USERNAME);

            // Assert
            verify(mailSender).send(messageCaptor.capture());
            String emailText = messageCaptor.getValue().getText();
            
            assertThat(emailText).contains(TEST_OTP);
        }

        @Test
        @DisplayName("Should personalize email with username")
        void sendOtpEmail_PersonalizedContent() {
            // Arrange
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act
            emailService.sendOtpEmail(TEST_EMAIL, TEST_OTP, "Jane Smith");

            // Assert
            verify(mailSender).send(messageCaptor.capture());
            String emailText = messageCaptor.getValue().getText();
            
            assertThat(emailText).contains("Jane Smith");
        }

        @Test
        @DisplayName("Should throw exception when mail sending fails")
        void sendOtpEmail_MailSendFailure() {
            // Arrange
            doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

            // Act & Assert
            assertThatThrownBy(() -> emailService.sendOtpEmail(TEST_EMAIL, TEST_OTP, TEST_USERNAME))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send verification email");
        }

        @Test
        @DisplayName("Should handle special characters in username")
        void sendOtpEmail_SpecialCharactersInUsername() {
            // Arrange
            String specialUsername = "José García-López";
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act
            emailService.sendOtpEmail(TEST_EMAIL, TEST_OTP, specialUsername);

            // Assert
            verify(mailSender).send(messageCaptor.capture());
            String emailText = messageCaptor.getValue().getText();
            
            assertThat(emailText).contains(specialUsername);
        }
    }

    @Nested
    @DisplayName("Email Format Tests")
    class EmailFormatTests {

        @Test
        @DisplayName("Should have correct subject line")
        void sendOtpEmail_CorrectSubject() {
            // Arrange
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act
            emailService.sendOtpEmail(TEST_EMAIL, TEST_OTP, TEST_USERNAME);

            // Assert
            verify(mailSender).send(messageCaptor.capture());
            String subject = messageCaptor.getValue().getSubject();
            
            assertThat(subject).isEqualTo("QuickBite - Email Verification Code");
        }

        @Test
        @DisplayName("Should have proper greeting")
        void sendOtpEmail_ProperGreeting() {
            // Arrange
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act
            emailService.sendOtpEmail(TEST_EMAIL, TEST_OTP, TEST_USERNAME);

            // Assert
            verify(mailSender).send(messageCaptor.capture());
            String emailText = messageCaptor.getValue().getText();
            
            assertThat(emailText).startsWith("Hi " + TEST_USERNAME);
        }

        @Test
        @DisplayName("Should include expiry warning")
        void sendOtpEmail_ExpiryWarning() {
            // Arrange
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act
            emailService.sendOtpEmail(TEST_EMAIL, TEST_OTP, TEST_USERNAME);

            // Assert
            verify(mailSender).send(messageCaptor.capture());
            String emailText = messageCaptor.getValue().getText();
            
            assertThat(emailText).contains("expire");
            assertThat(emailText).contains("5 minutes");
        }

        @Test
        @DisplayName("Should include security disclaimer")
        void sendOtpEmail_SecurityDisclaimer() {
            // Arrange
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act
            emailService.sendOtpEmail(TEST_EMAIL, TEST_OTP, TEST_USERNAME);

            // Assert
            verify(mailSender).send(messageCaptor.capture());
            String emailText = messageCaptor.getValue().getText();
            
            assertThat(emailText).containsIgnoringCase("didn't request");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty username")
        void sendOtpEmail_EmptyUsername() {
            // Arrange
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act
            emailService.sendOtpEmail(TEST_EMAIL, TEST_OTP, "");

            // Assert - Should still send, just with empty name
            verify(mailSender).send(messageCaptor.capture());
        }

        @Test
        @DisplayName("Should handle long OTP code")
        void sendOtpEmail_LongOtpCode() {
            // Arrange
            String longOtp = "12345678901234567890";
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act
            emailService.sendOtpEmail(TEST_EMAIL, longOtp, TEST_USERNAME);

            // Assert
            verify(mailSender).send(messageCaptor.capture());
            assertThat(messageCaptor.getValue().getText()).contains(longOtp);
        }
    }
}
