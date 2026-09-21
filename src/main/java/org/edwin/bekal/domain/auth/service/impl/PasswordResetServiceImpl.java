package org.edwin.bekal.domain.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.service.Impl.EmailServiceImpl;
import org.edwin.bekal.domain.auth.entity.PasswordReset;
import org.edwin.bekal.domain.auth.repository.PasswordResetRepository;
import org.edwin.bekal.domain.auth.service.PasswordResetService;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int OTP_LENGTH = 6;
    private static final long OTP_VALID_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CustomerRepository customerRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailService;

    @Override
    @Transactional
    public void forgotPassword(String email) {
        Customer customer = customerRepository.findByCustomerEmail(email).orElse(null);

        // Selalu dianggap sukses walau email tidak terdaftar, supaya tidak
        // bocorkan informasi email mana yang ada di sistem (anti-enumeration).
        if (customer == null) {
            return;
        }

        passwordResetRepository.deleteByEmailAndUsedAtIsNull(email);

        String otp = generateOtp();

        PasswordReset resetEntry = PasswordReset.builder()
                .userType("CUSTOMER")
                .userId(customer.getId())
                .email(email)
                .token(otp)
                .expiresAt(Instant.now().plus(OTP_VALID_MINUTES, ChronoUnit.MINUTES))
                .createdAt(Instant.now())
                .build();

        passwordResetRepository.save(resetEntry);
        emailService.sendOtpEmail(email, otp);
    }

    @Override
    @Transactional(readOnly = true)
    public void verifyOtp(String email, String otp) {
        getValidResetEntry(email, otp);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        PasswordReset resetEntry = getValidResetEntry(email, otp);

        Customer customer = customerRepository.findByCustomerEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Akun tidak ditemukan"));

        customer.setCustomerPasswordHash(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);

        resetEntry.setUsedAt(Instant.now());
        passwordResetRepository.save(resetEntry);
    }

    private PasswordReset getValidResetEntry(String email, String otp) {
        PasswordReset resetEntry = passwordResetRepository
                .findTopByEmailAndTokenAndUsedAtIsNullOrderByCreatedAtDesc(email, otp)
                .orElseThrow(() -> new IllegalArgumentException("Kode OTP tidak valid"));

        if (resetEntry.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Kode OTP sudah kedaluwarsa, silakan minta kode baru");
        }

        return resetEntry;
    }

    private String generateOtp() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }
}