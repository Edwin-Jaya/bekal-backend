package org.edwin.bekal.domain.auth.service;

public interface PasswordResetService {
    void forgotPassword(String email);
    void verifyOtp(String email, String otp);
    void resetPassword(String email, String otp, String newPassword);
}
