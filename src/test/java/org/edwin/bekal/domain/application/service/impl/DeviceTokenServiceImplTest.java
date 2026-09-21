package org.edwin.bekal.domain.application.service.impl;

import org.edwin.bekal.domain.application.entity.DeviceToken;
import org.edwin.bekal.domain.application.repository.DeviceTokenRepository;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceImplTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private DeviceTokenServiceImpl deviceTokenService;

    @Test
    @DisplayName("registerToken - Throws IllegalArgumentException when customer is not found")
    void registerToken_customerNotFound_throwsException() {
        UUID customerId = UUID.randomUUID();
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> deviceTokenService.registerToken(customerId, "fcm_token_123", "Android Pixel 7"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer tidak ditemukan");

        verify(customerRepository).findById(customerId);
        verifyNoInteractions(deviceTokenRepository);
    }

    @Test
    @DisplayName("registerToken - Creates new device token when token does not exist")
    void registerToken_newToken_success() {
        UUID customerId = UUID.randomUUID();
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(mockCustomer));
        given(deviceTokenRepository.findByFcmToken("fcm_token_123")).willReturn(Optional.empty());

        deviceTokenService.registerToken(customerId, "fcm_token_123", "Android Pixel 7");

        verify(customerRepository).findById(customerId);
        verify(deviceTokenRepository).findByFcmToken("fcm_token_123");
        verify(deviceTokenRepository).saveAndFlush(argThat(token ->
                token.getCustomer().equals(mockCustomer) &&
                        "fcm_token_123".equals(token.getFcmToken()) &&
                        "Android Pixel 7".equals(token.getDeviceInfo()) &&
                        token.getCreatedAt() != null &&
                        token.getUpdatedAt() != null
        ));
    }

    @Test
    @DisplayName("registerToken - Updates existing device token without overwriting createdAt")
    void registerToken_existingToken_success() {
        UUID customerId = UUID.randomUUID();
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);

        Instant existingCreatedAt = Instant.now().minusSeconds(3600);
        DeviceToken existingToken = new DeviceToken();
        existingToken.setFcmToken("fcm_token_123");
        existingToken.setCreatedAt(existingCreatedAt);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(mockCustomer));
        given(deviceTokenRepository.findByFcmToken("fcm_token_123")).willReturn(Optional.of(existingToken));

        deviceTokenService.registerToken(customerId, "fcm_token_123", "iPhone 14 Pro");

        verify(customerRepository).findById(customerId);
        verify(deviceTokenRepository).findByFcmToken("fcm_token_123");
        verify(deviceTokenRepository).saveAndFlush(argThat(token ->
                token.getCustomer().equals(mockCustomer) &&
                        "fcm_token_123".equals(token.getFcmToken()) &&
                        "iPhone 14 Pro".equals(token.getDeviceInfo()) &&
                        existingCreatedAt.equals(token.getCreatedAt()) &&
                        token.getUpdatedAt() != null
        ));
    }

    @Test
    @DisplayName("registerToken - Retries saveOrUpdate when DataIntegrityViolationException occurs on first attempt")
    void registerToken_concurrencyException_retriesAndSucceeds() {
        UUID customerId = UUID.randomUUID();
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(mockCustomer));
        given(deviceTokenRepository.findByFcmToken("fcm_token_123")).willReturn(Optional.empty());

        // Percobaan 1: Lempar DataIntegrityViolationException
        // Percobaan 2 (Retry): Berhasil menyimpan (mengembalikan objek argument)
        given(deviceTokenRepository.saveAndFlush(any(DeviceToken.class)))
                .willThrow(new DataIntegrityViolationException("Duplicate entry"))
                .willAnswer(invocation -> invocation.getArgument(0));

        deviceTokenService.registerToken(customerId, "fcm_token_123", "Android Pixel 7");

        verify(customerRepository).findById(customerId);
        verify(deviceTokenRepository, times(2)).findByFcmToken("fcm_token_123");
        verify(deviceTokenRepository, times(2)).saveAndFlush(any(DeviceToken.class));
    }
}