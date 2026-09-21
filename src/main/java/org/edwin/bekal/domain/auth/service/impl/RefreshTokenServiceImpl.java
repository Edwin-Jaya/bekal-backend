package org.edwin.bekal.domain.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.auth.entity.RefreshToken;
import org.edwin.bekal.domain.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-days:30}")
    private int refreshTokenExpirationDays;

    @Transactional
    public RefreshToken createRefreshToken(UUID userId, String userType) {
        // Jika token lama untuk user_id & user_type ini sudah ada, timpa tokennya
        RefreshToken refreshToken = refreshTokenRepository.findByUserIdAndUserType(userId, userType)
                .orElseGet(RefreshToken::new);

        Instant now = Instant.now();

        refreshToken.setUserId(userId);
        refreshToken.setUserType(userType);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS));

        if (refreshToken.getCreatedAt() == null) {
            refreshToken.setCreatedAt(now);
        }

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken verifyExpirationAndStatus(RefreshToken token) {
        // Cek status is_revoked[cite: 1]
        if (token.isRevoked()) {
            throw new IllegalArgumentException("Refresh token tidak valid atau sudah dicabut (revoked)");
        }

        // Cek tanggal kadaluwarsa
        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setRevoked(true); // Tandai revoked jika sudah expired
            refreshTokenRepository.save(token);
            throw new IllegalArgumentException("Refresh token telah kedaluwarsa. Silakan login kembali.");
        }

        return token;
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }
}
