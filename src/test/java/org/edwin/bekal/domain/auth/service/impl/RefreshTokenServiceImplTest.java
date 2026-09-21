package org.edwin.bekal.domain.auth.service.impl;

import org.edwin.bekal.domain.auth.entity.RefreshToken;
import org.edwin.bekal.domain.auth.repository.RefreshTokenRepository;
import org.edwin.bekal.domain.auth.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationDays", 30);
    }

    @Nested
    @DisplayName("createRefreshToken Tests")
    class CreateRefreshTokenTests {

        @Test
        @DisplayName("Should create new refresh token when no existing token found")
        void createRefreshToken_newEntity_success() {
            UUID userId = UUID.randomUUID();
            String userType = "CUSTOMER";

            given(refreshTokenRepository.findByUserIdAndUserType(userId, userType)).willReturn(Optional.empty());
            given(refreshTokenRepository.save(any(RefreshToken.class))).willAnswer(inv -> inv.getArgument(0));

            RefreshToken result = refreshTokenService.createRefreshToken(userId, userType);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getUserType()).isEqualTo(userType);
            assertThat(result.getToken()).isNotNull();
            assertThat(result.isRevoked()).isFalse();
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getExpiresAt()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("Should update existing refresh token when found")
        void createRefreshToken_existingEntity_updatesToken() {
            UUID userId = UUID.randomUUID();
            String userType = "CUSTOMER";

            RefreshToken existingToken = new RefreshToken();
            existingToken.setUserId(userId);
            existingToken.setUserType(userType);
            existingToken.setToken("old-token");
            existingToken.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS));

            given(refreshTokenRepository.findByUserIdAndUserType(userId, userType)).willReturn(Optional.of(existingToken));
            given(refreshTokenRepository.save(any(RefreshToken.class))).willAnswer(inv -> inv.getArgument(0));

            RefreshToken result = refreshTokenService.createRefreshToken(userId, userType);

            assertThat(result.getToken()).isNotEqualTo("old-token");
            verify(refreshTokenRepository).save(existingToken);
        }
    }

    @Nested
    @DisplayName("verifyExpirationAndStatus Tests")
    class VerifyExpirationAndStatusTests {

        @Test
        @DisplayName("Should return token when token is valid and active")
        void verifyExpirationAndStatus_validToken_returnsToken() {
            RefreshToken token = new RefreshToken();
            token.setRevoked(false);
            token.setExpiresAt(Instant.now().plus(10, ChronoUnit.DAYS));

            RefreshToken result = refreshTokenService.verifyExpirationAndStatus(token);

            assertThat(result).isEqualTo(token);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when token is revoked")
        void verifyExpirationAndStatus_revokedToken_throwsException() {
            RefreshToken token = new RefreshToken();
            token.setRevoked(true);

            assertThatThrownBy(() -> refreshTokenService.verifyExpirationAndStatus(token))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Refresh token tidak valid atau sudah dicabut (revoked)");
        }

        @Test
        @DisplayName("Should revoke token and throw IllegalArgumentException when token is expired")
        void verifyExpirationAndStatus_expiredToken_revokesAndThrowsException() {
            RefreshToken token = new RefreshToken();
            token.setRevoked(false);
            token.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));

            assertThatThrownBy(() -> refreshTokenService.verifyExpirationAndStatus(token))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Refresh token telah kedaluwarsa. Silakan login kembali.");

            assertThat(token.isRevoked()).isTrue();
            verify(refreshTokenRepository).save(token);
        }
    }

    @Nested
    @DisplayName("findByToken and revokeToken Tests")
    class FindAndRevokeTests {

        @Test
        @DisplayName("findByToken - Should call repository")
        void findByToken_success() {
            String tokenVal = "token-xyz";
            RefreshToken token = new RefreshToken();
            given(refreshTokenRepository.findByToken(tokenVal)).willReturn(Optional.of(token));

            Optional<RefreshToken> result = refreshTokenService.findByToken(tokenVal);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(token);
        }

        @Test
        @DisplayName("revokeToken - Should set revoked to true if token exists")
        void revokeToken_tokenExists_setsRevoked() {
            String tokenVal = "token-xyz";
            RefreshToken token = new RefreshToken();
            token.setRevoked(false);

            given(refreshTokenRepository.findByToken(tokenVal)).willReturn(Optional.of(token));

            refreshTokenService.revokeToken(tokenVal);

            assertThat(token.isRevoked()).isTrue();
            verify(refreshTokenRepository).save(token);
        }

        @Test
        @DisplayName("revokeToken - Should do nothing if token does not exist")
        void revokeToken_tokenNotFound_doesNothing() {
            String tokenVal = "unknown-token";
            given(refreshTokenRepository.findByToken(tokenVal)).willReturn(Optional.empty());

            refreshTokenService.revokeToken(tokenVal);

            verify(refreshTokenRepository, never()).save(any());
        }
    }
}