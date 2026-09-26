package org.edwin.bekal.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.config.security.JwtTokenProvider;
import org.edwin.bekal.domain.auth.dto.*;
import org.edwin.bekal.domain.auth.entity.RefreshToken;
import org.edwin.bekal.domain.auth.service.PasswordResetService;
import org.edwin.bekal.domain.auth.service.impl.GoogleAuthServiceImpl;
import org.edwin.bekal.domain.auth.service.impl.RefreshTokenServiceImpl;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user & customer authentication, token refresh, Google OAuth, and password reset")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordResetService passwordResetService;
    private final GoogleAuthServiceImpl googleAuthService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final CustomerRepository customerRepository;

    @Operation(summary = "Customer Login", description = "Authenticates a customer with email and password, returning JWT access token and refresh token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid credentials or request format"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Bad credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);

        Customer customer = customerRepository.findByCustomerEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Customer tidak ditemukan"));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(customer.getId(), "CUSTOMER");

        JwtResponse response = JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken.getToken())
                .type("Bearer")
                .build();

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @Operation(summary = "Internal Employee Login", description = "Authenticates internal staff (Branch Manager, Back Office, Marketing, Admin) with email and password.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Bad credentials")
    })
    @PostMapping("/login-employee")
    public ResponseEntity<ApiResponse<JwtResponse>> loginEmployee(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(ApiResponse.success("Login successful", new JwtResponse(jwt, "Bearer")));
    }

    @Operation(summary = "Forgot Password Request", description = "Initiates password reset by sending a 6-digit OTP to the customer's email address.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent if email exists")
    })
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getEmail());
        return ApiResponse.success("Jika email terdaftar, kode OTP telah dikirim", null);
    }

    @Operation(summary = "Verify OTP", description = "Validates the 6-digit OTP code sent for password reset.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP is valid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("/verify-otp")
    public ApiResponse<Void> verifyOtp(@RequestBody VerifyOtpRequest request) {
        passwordResetService.verifyOtp(request.getEmail(), request.getOtp());
        return ApiResponse.success("Kode OTP valid", null);
    }

    @Operation(summary = "Reset Password", description = "Sets a new password using verified OTP code.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid OTP or request")
    })
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ApiResponse.success("Kata sandi berhasil diubah", null);
    }

    @Operation(summary = "Google OAuth Authentication", description = "Authenticates or registers customer via Google OAuth ID token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Google authentication successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid Google token")
    })
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<GoogleAuthResponse>> authenticateGoogle(@RequestBody GoogleAuthRequest request) {
        GoogleAuthResponse response = googleAuthService.authenticateWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.success("Login Google berhasil", response));
    }

    @Operation(summary = "Refresh Access Token", description = "Generates a new access token using a valid refresh token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpirationAndStatus)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token tidak valid atau tidak ditemukan"));

        Customer customer = customerRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Customer tidak ditemukan"));

        String newAccessToken = tokenProvider.generateTokenForCustomer(customer);

        JwtResponse response = JwtResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .type("Bearer")
                .build();

        return ResponseEntity.ok(ApiResponse.success("Token berhasil diperbarui", response));
    }

    @Operation(summary = "Logout", description = "Revokes the active refresh token and signs out the user.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.revokeToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logout berhasil", null));
    }
}