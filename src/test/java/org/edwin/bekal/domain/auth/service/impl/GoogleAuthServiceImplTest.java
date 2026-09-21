package org.edwin.bekal.domain.auth.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.edwin.bekal.config.security.JwtTokenProvider;
import org.edwin.bekal.domain.auth.dto.GoogleAuthRequest;
import org.edwin.bekal.domain.auth.dto.GoogleAuthResponse;
import org.edwin.bekal.domain.auth.entity.RefreshToken;
import org.edwin.bekal.domain.auth.service.impl.GoogleAuthServiceImpl;
import org.edwin.bekal.domain.auth.service.impl.RefreshTokenServiceImpl;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.enums.CustomerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private RefreshTokenServiceImpl refreshTokenService;

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private FirebaseToken firebaseToken;

    @InjectMocks
    private GoogleAuthServiceImpl googleAuthService;

    @Nested
    @DisplayName("authenticateWithGoogle Tests")
    class AuthenticateWithGoogleTests {

        @Test
        @DisplayName("Should authenticate successfully with valid Google Firebase token")
        void authenticateWithGoogle_success() throws Exception {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setFirebaseToken("valid-firebase-token");

            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setId(customerId);
            customer.setCustomerEmail("google@example.com");
            customer.setCustomerStatus(CustomerStatus.ACTIVE);

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken("refresh-token-val");

            given(firebaseToken.getEmail()).willReturn("google@example.com");
            given(firebaseAuth.verifyIdToken("valid-firebase-token")).willReturn(firebaseToken);
            given(customerRepository.findByCustomerEmail("google@example.com")).willReturn(Optional.of(customer));
            given(tokenProvider.generateTokenForCustomer(customer)).willReturn("jwt-token-val");
            given(refreshTokenService.createRefreshToken(customerId, "CUSTOMER")).willReturn(refreshToken);

            try (MockedStatic<FirebaseAuth> staticFirebaseAuth = mockStatic(FirebaseAuth.class)) {
                staticFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);

                GoogleAuthResponse response = googleAuthService.authenticateWithGoogle(request);

                assertThat(response).isNotNull();
                assertThat(response.getToken()).isEqualTo("jwt-token-val");
                assertThat(response.getRefreshToken()).isEqualTo("refresh-token-val");
                assertThat(response.getType()).isEqualTo("GOOGLE");

                verify(customerRepository).save(customer);
            }
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when email in Firebase token is blank")
        void authenticateWithGoogle_blankEmail_throwsException() throws Exception {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setFirebaseToken("valid-firebase-token");

            given(firebaseToken.getEmail()).willReturn("");
            given(firebaseAuth.verifyIdToken("valid-firebase-token")).willReturn(firebaseToken);

            try (MockedStatic<FirebaseAuth> staticFirebaseAuth = mockStatic(FirebaseAuth.class)) {
                staticFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);

                assertThatThrownBy(() -> googleAuthService.authenticateWithGoogle(request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Token Firebase tidak memuat email yang valid");
            }
        }

        @Test
        @DisplayName("Should throw IllegalStateException when email is not registered")
        void authenticateWithGoogle_unregisteredEmail_throwsException() throws Exception {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setFirebaseToken("valid-firebase-token");

            given(firebaseToken.getEmail()).willReturn("unregistered@example.com");
            given(firebaseAuth.verifyIdToken("valid-firebase-token")).willReturn(firebaseToken);
            given(customerRepository.findByCustomerEmail("unregistered@example.com")).willReturn(Optional.empty());

            try (MockedStatic<FirebaseAuth> staticFirebaseAuth = mockStatic(FirebaseAuth.class)) {
                staticFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);

                assertThatThrownBy(() -> googleAuthService.authenticateWithGoogle(request))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Email belum terdaftar, silakan daftar terlebih dahulu");
            }
        }

        @Test
        @DisplayName("Should throw IllegalStateException when customer account is inactive")
        void authenticateWithGoogle_inactiveAccount_throwsException() throws Exception {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setFirebaseToken("valid-firebase-token");

            Customer customer = new Customer();
            customer.setCustomerEmail("inactive@example.com");
            customer.setCustomerStatus(CustomerStatus.INACTIVE);

            given(firebaseToken.getEmail()).willReturn("inactive@example.com");
            given(firebaseAuth.verifyIdToken("valid-firebase-token")).willReturn(firebaseToken);
            given(customerRepository.findByCustomerEmail("inactive@example.com")).willReturn(Optional.of(customer));

            try (MockedStatic<FirebaseAuth> staticFirebaseAuth = mockStatic(FirebaseAuth.class)) {
                staticFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);

                assertThatThrownBy(() -> googleAuthService.authenticateWithGoogle(request))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Akun pelanggan tidak aktif");
            }
        }

        @Test
        @DisplayName("Should wrap Firebase Exception into IllegalArgumentException")
        void authenticateWithGoogle_firebaseError_wrapsException() throws Exception {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setFirebaseToken("invalid-firebase-token");

            given(firebaseAuth.verifyIdToken("invalid-firebase-token")).willThrow(new RuntimeException("Token expired"));

            try (MockedStatic<FirebaseAuth> staticFirebaseAuth = mockStatic(FirebaseAuth.class)) {
                staticFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);

                assertThatThrownBy(() -> googleAuthService.authenticateWithGoogle(request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Verifikasi token Google gagal: Token expired");
            }
        }
    }
}