package org.edwin.bekal.domain.auth.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.config.security.JwtTokenProvider;
import org.edwin.bekal.domain.auth.dto.GoogleAuthRequest;
import org.edwin.bekal.domain.auth.dto.GoogleAuthResponse;
import org.edwin.bekal.domain.auth.entity.RefreshToken;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.enums.CustomerStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl {

    private final CustomerRepository customerRepository;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenServiceImpl refreshTokenService;

    @Transactional
    public GoogleAuthResponse authenticateWithGoogle(GoogleAuthRequest request) {
        try {
            // 1. Verifikasi ID Token Firebase
            FirebaseToken decodedToken = FirebaseAuth.getInstance()
                    .verifyIdToken(request.getFirebaseToken());

            String email = decodedToken.getEmail();

            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Token Firebase tidak memuat email yang valid");
            }

            // 2. Cari customer (throw jika belum terdaftar)
            Customer customer = customerRepository.findByCustomerEmail(email)
                    .orElseThrow(() -> new IllegalStateException("Email belum terdaftar, silakan daftar terlebih dahulu"));

            // 3. Cek status aktif
            if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
                throw new IllegalStateException("Akun pelanggan tidak aktif");
            }

            // 4. Update last login
            customer.setCustomerLastLoginAt(Instant.now());
            customerRepository.save(customer);

            // 5. Generate Access Token (JWT) & Refresh Token
            String jwt = tokenProvider.generateTokenForCustomer(customer);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(customer.getId(), "CUSTOMER");

            return GoogleAuthResponse.builder()
                    .token(jwt)
                    .refreshToken(refreshToken.getToken())
                    .type("GOOGLE")
                    .build();

        } catch (IllegalStateException e) {
            throw e; // re-throw agar ditangkap Controller Exception Handler
        } catch (Exception e) {
            throw new IllegalArgumentException("Verifikasi token Google gagal: " + e.getMessage(), e);
        }
    }
}