package org.edwin.bekal.domain.auth.controller;

import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.config.security.JwtTokenProvider;
import org.edwin.bekal.domain.auth.controller.AuthController;
import org.edwin.bekal.domain.auth.dto.*;
import org.edwin.bekal.domain.auth.entity.RefreshToken;
import org.edwin.bekal.domain.auth.service.PasswordResetService;
import org.edwin.bekal.domain.auth.service.impl.GoogleAuthServiceImpl;
import org.edwin.bekal.domain.auth.service.impl.RefreshTokenServiceImpl;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private GoogleAuthServiceImpl googleAuthService;

    @Mock
    private RefreshTokenServiceImpl refreshTokenService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AuthController authController;

    @Nested
    @DisplayName("login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should authenticate customer successfully and return JwtResponse with refresh token")
        void login_success() {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("password123");

            Authentication authentication = mock(Authentication.class);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
            given(tokenProvider.generateToken(authentication)).willReturn("jwt-access-token");

            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setId(customerId);
            given(customerRepository.findByCustomerEmail("user@example.com")).willReturn(Optional.of(customer));

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken("refresh-token-123");
            given(refreshTokenService.createRefreshToken(customerId, "CUSTOMER")).willReturn(refreshToken);

            ResponseEntity<ApiResponse<JwtResponse>> responseEntity = authController.login(request);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody()).isNotNull();
            assertThat(responseEntity.getBody().getSuccess()).isTrue();

            JwtResponse data = responseEntity.getBody().getData();
            assertThat(data.getToken()).isEqualTo("jwt-access-token");
            assertThat(data.getRefreshToken()).isEqualTo("refresh-token-123");
            assertThat(data.getType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when customer email is not found")
        void login_customerNotFound_throwsException() {
            LoginRequest request = new LoginRequest();
            request.setEmail("notfound@example.com");
            request.setPassword("password123");

            Authentication authentication = mock(Authentication.class);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
            given(tokenProvider.generateToken(authentication)).willReturn("jwt-access-token");
            given(customerRepository.findByCustomerEmail("notfound@example.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authController.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Customer tidak ditemukan");
        }
    }

    @Nested
    @DisplayName("loginEmployee Tests")
    class LoginEmployeeTests {

        @Test
        @DisplayName("Should authenticate employee successfully")
        void loginEmployee_success() {
            LoginRequest request = new LoginRequest();
            request.setEmail("emp@example.com");
            request.setPassword("password123");

            Authentication authentication = mock(Authentication.class);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
            given(tokenProvider.generateToken(authentication)).willReturn("jwt-emp-token");

            ResponseEntity<ApiResponse<JwtResponse>> responseEntity = authController.loginEmployee(request);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody()).isNotNull();

            JwtResponse data = responseEntity.getBody().getData();
            assertThat(data.getToken()).isEqualTo("jwt-emp-token");
            assertThat(data.getType()).isEqualTo("Bearer");
        }
    }

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("forgotPassword - Should invoke passwordResetService")
        void forgotPassword_success() {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("user@example.com");

            ApiResponse<Void> response = authController.forgotPassword(request);

            verify(passwordResetService).forgotPassword("user@example.com");
            assertThat(response.getSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Jika email terdaftar, kode OTP telah dikirim");
        }

        @Test
        @DisplayName("verifyOtp - Should invoke passwordResetService")
        void verifyOtp_success() {
            VerifyOtpRequest request = new VerifyOtpRequest();
            request.setEmail("user@example.com");
            request.setOtp("123456");

            ApiResponse<Void> response = authController.verifyOtp(request);

            verify(passwordResetService).verifyOtp("user@example.com", "123456");
            assertThat(response.getSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Kode OTP valid");
        }

        @Test
        @DisplayName("resetPassword - Should invoke passwordResetService")
        void resetPassword_success() {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setEmail("user@example.com");
            request.setOtp("123456");
            request.setNewPassword("newSecret123");

            ApiResponse<Void> response = authController.resetPassword(request);

            verify(passwordResetService).resetPassword("user@example.com", "123456", "newSecret123");
            assertThat(response.getSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Kata sandi berhasil diubah");
        }
    }

    @Nested
    @DisplayName("authenticateGoogle Tests")
    class AuthenticateGoogleTests {

        @Test
        @DisplayName("Should process Google authentication successfully")
        void authenticateGoogle_success() {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setFirebaseToken("sample-firebase-token");

            GoogleAuthResponse googleResponse = GoogleAuthResponse.builder()
                    .token("jwt-google-token")
                    .refreshToken("refresh-google-token")
                    .type("GOOGLE")
                    .build();

            given(googleAuthService.authenticateWithGoogle(request)).willReturn(googleResponse);

            ResponseEntity<ApiResponse<GoogleAuthResponse>> responseEntity = authController.authenticateGoogle(request);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody()).isNotNull();
            assertThat(responseEntity.getBody().getData().getToken()).isEqualTo("jwt-google-token");
        }
    }

    @Nested
    @DisplayName("refreshToken Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh JWT token successfully")
        void refreshToken_success() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("valid-refresh-token");

            UUID customerId = UUID.randomUUID();
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken("valid-refresh-token");
            refreshToken.setUserId(customerId);

            Customer customer = new Customer();
            customer.setId(customerId);

            given(refreshTokenService.findByToken("valid-refresh-token")).willReturn(Optional.of(refreshToken));
            given(refreshTokenService.verifyExpirationAndStatus(refreshToken)).willReturn(refreshToken);
            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(tokenProvider.generateTokenForCustomer(customer)).willReturn("new-jwt-token");

            ResponseEntity<ApiResponse<JwtResponse>> responseEntity = authController.refreshToken(request);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody()).isNotNull();

            JwtResponse data = responseEntity.getBody().getData();
            assertThat(data.getToken()).isEqualTo("new-jwt-token");
            assertThat(data.getRefreshToken()).isEqualTo("valid-refresh-token");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when refreshToken is invalid or missing")
        void refreshToken_invalidToken_throwsException() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("invalid-refresh-token");

            given(refreshTokenService.findByToken("invalid-refresh-token")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authController.refreshToken(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Refresh token tidak valid atau tidak ditemukan");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when customer does not exist for the refresh token")
        void refreshToken_customerNotFound_throwsException() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("valid-refresh-token");

            UUID customerId = UUID.randomUUID();
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUserId(customerId);

            given(refreshTokenService.findByToken("valid-refresh-token")).willReturn(Optional.of(refreshToken));
            given(refreshTokenService.verifyExpirationAndStatus(refreshToken)).willReturn(refreshToken);
            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> authController.refreshToken(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Customer tidak ditemukan");
        }
    }

    @Nested
    @DisplayName("logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should revoke token successfully on logout")
        void logout_success() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("token-to-revoke");

            ResponseEntity<ApiResponse<Void>> responseEntity = authController.logout(request);

            verify(refreshTokenService).revokeToken("token-to-revoke");
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody()).isNotNull();
            assertThat(responseEntity.getBody().getMessage()).isEqualTo("Logout berhasil");
        }
    }
}