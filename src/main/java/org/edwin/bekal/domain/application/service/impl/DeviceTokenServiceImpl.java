package org.edwin.bekal.domain.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.entity.DeviceToken;
import org.edwin.bekal.domain.application.repository.DeviceTokenRepository;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceTokenServiceImpl {

    private final DeviceTokenRepository deviceTokenRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public void registerToken(UUID customerId, String fcmToken, String deviceInfo) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer tidak ditemukan"));

        try {
            saveOrUpdateDeviceToken(customer, fcmToken, deviceInfo);
        } catch (DataIntegrityViolationException e) {
            // Mencegah crash jika 2 thread HTTP (exec-4 & exec-6) mengecek DB di saat bersamaan
            saveOrUpdateDeviceToken(customer, fcmToken, deviceInfo);
        }
    }

    private void saveOrUpdateDeviceToken(Customer customer, String fcmToken, String deviceInfo) {
        DeviceToken deviceToken = deviceTokenRepository.findByFcmToken(fcmToken)
                .orElseGet(DeviceToken::new);

        deviceToken.setCustomer(customer);
        deviceToken.setFcmToken(fcmToken);
        deviceToken.setDeviceInfo(deviceInfo);

        Instant now = Instant.now();
        deviceToken.setUpdatedAt(now);
        if (deviceToken.getCreatedAt() == null) {
            deviceToken.setCreatedAt(now);
        }

        deviceTokenRepository.saveAndFlush(deviceToken);
    }
}