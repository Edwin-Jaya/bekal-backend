package org.edwin.bekal.domain.auth.service.impl;

import org.edwin.bekal.common.service.Impl.EmailServiceImpl;
import org.edwin.bekal.domain.auth.entity.PasswordReset;
import org.edwin.bekal.domain.auth.repository.PasswordResetRepository;
import org.edwin.bekal.domain.auth.service.impl.PasswordResetServiceImpl;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailServiceImpl emailService;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    @Nested
    @DisplayName("forgotPassword Tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should return quietly without sending email if customer does not exist (Anti-enumeration)")
        void forgotPassword_customerNotFound_doesNothing() {
            given(customerRepository.findByCustomerEmail("unknown@example.com")).willReturn(Optional.empty());

            passwordResetService.forgotPassword("unknown@example.com");

            verify(passwordResetRepository, never()).deleteByEmailAndUsedAtIsNull(any());
            verify(passwordResetRepository, never()).save(any());
            verify(emailService, never()).sendOtpEmail(any(), any());
        }

        @Test
        @DisplayName("Should create OTP entry and send email when customer exists")
        void forgotPassword_customerFound_createsOtpAndSendsEmail() {
            String email = "found@example.com";
            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setId(customerId);

            given(customerRepository.findByCustomerEmail(email)).willReturn(Optional.of(customer));

            passwordResetService.forgotPassword(email);

            verify(passwordResetRepository).deleteByEmailAndUsedAtIsNull(email);
            verify(passwordResetRepository).save(any(PasswordReset.class));
            verify(emailService).sendOtpEmail(eq(email), anyString());
        }
    }

    @Nested
    @DisplayName("verifyOtp Tests")
    class VerifyOtpTests {

        @Test
        @DisplayName("Should complete quietly when OTP is valid and not expired")
        void verifyOtp_validOtp_success() {
            String email = "test@example.com";
            String otp = "123456";

            PasswordReset resetEntry = PasswordReset.builder()
                    .email(email)
                    .token(otp)
                    .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                    .build();

            given(passwordResetRepository.findTopByEmailAndTokenAndUsedAtIsNullOrderByCreatedAtDesc(email, otp))
                    .willReturn(Optional.of(resetEntry));

            passwordResetService.verifyOtp(email, otp);

            verify(passwordResetRepository).findTopByEmailAndTokenAndUsedAtIsNullOrderByCreatedAtDesc(email, otp);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when OTP is not found")
        void verifyOtp_otpNotFound_throwsException() {
            given(passwordResetRepository.findTopByEmailAndTokenAndUsedAtIsNullOrderByCreatedAtDesc("test@example.com", "999999"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetService.verifyOtp("test@example.com", "999999"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Kode OTP tidak valid");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when OTP has expired")
        void verifyOtp_expiredOtp_throwsException() {
            String email = "test@example.com";
            String otp = "123456";

            PasswordReset resetEntry = PasswordReset.builder()
                    .email(email)
                    .token(otp)
                    .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES)) // Expired
                    .build();

            given(passwordResetRepository.findTopByEmailAndTokenAndUsedAtIsNullOrderByCreatedAtDesc(email, otp))
                    .willReturn(Optional.of(resetEntry));

            assertThatThrownBy(() -> passwordResetService.verifyOtp(email, otp))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Kode OTP sudah kedaluwarsa, silakan minta kode baru");
        }
    }

    @Nested
    @DisplayName("resetPassword Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should update password and mark OTP as used")
        void resetPassword_success() {
            String email = "test@example.com";
            String otp = "123456";
            String newPassword = "newPassword123";

            PasswordReset resetEntry = PasswordReset.builder()
                    .email(email)
                    .token(otp)
                    .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                    .build();

            Customer customer = new Customer();
            customer.setCustomerEmail(email);

            given(passwordResetRepository.findTopByEmailAndTokenAndUsedAtIsNullOrderByCreatedAtDesc(email, otp))
                    .willReturn(Optional.of(resetEntry));
            given(customerRepository.findByCustomerEmail(email)).willReturn(Optional.of(customer));
            given(passwordEncoder.encode(newPassword)).willReturn("encodedPassword123");

            passwordResetService.resetPassword(email, otp, newPassword);

            assertThat(customer.getCustomerPasswordHash()).isEqualTo("encodedPassword123");
            assertThat(resetEntry.getUsedAt()).isNotNull();

            verify(customerRepository).save(customer);
            verify(passwordResetRepository).save(resetEntry);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when account is not found during reset")
        void resetPassword_customerNotFound_throwsException() {
            String email = "test@example.com";
            String otp = "123456";

            PasswordReset resetEntry = PasswordReset.builder()
                    .email(email)
                    .token(otp)
                    .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                    .build();

            given(passwordResetRepository.findTopByEmailAndTokenAndUsedAtIsNullOrderByCreatedAtDesc(email, otp))
                    .willReturn(Optional.of(resetEntry));
            given(customerRepository.findByCustomerEmail(email)).willReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetService.resetPassword(email, otp, "newPassword123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Akun tidak ditemukan");
        }
    }
}