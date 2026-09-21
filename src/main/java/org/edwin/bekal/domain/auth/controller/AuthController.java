package org.edwin.bekal.domain.auth.controller;

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
import org.springframework.http.ResponseCookie;
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
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordResetService passwordResetService;
    private final GoogleAuthServiceImpl googleAuthService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final CustomerRepository customerRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);

        // 1. Ambil data customer untuk dapatkan ID-nya
        Customer customer = customerRepository.findByCustomerEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Customer tidak ditemukan"));

        // 2. Buat refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(customer.getId(), "CUSTOMER");

        // 3. Retur keduanya
        JwtResponse response = JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken.getToken())
                .type("Bearer")
                .build();

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/login-employee")
    public ResponseEntity<ApiResponse<JwtResponse>> loginEmployee(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );


        String jwt = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(ApiResponse.success("Login successful", new JwtResponse(jwt, "Bearer")));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getEmail());
        return ApiResponse.success("Jika email terdaftar, kode OTP telah dikirim", null);
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Void> verifyOtp(@RequestBody VerifyOtpRequest request) {
        passwordResetService.verifyOtp(request.getEmail(), request.getOtp());
        return ApiResponse.success("Kode OTP valid", null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ApiResponse.success("Kata sandi berhasil diubah", null);
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<GoogleAuthResponse>> authenticateGoogle(@RequestBody GoogleAuthRequest request) {
        GoogleAuthResponse response = googleAuthService.authenticateWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.success("Login Google berhasil", response));
    }

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

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.revokeToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logout berhasil", null));
    }
}