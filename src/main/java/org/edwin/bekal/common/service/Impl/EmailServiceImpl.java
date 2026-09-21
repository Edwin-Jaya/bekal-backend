package org.edwin.bekal.common.service.Impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Kode Reset Password - BEKAL");
            helper.setText(
                    "<div style=\"font-family:sans-serif\">" +
                            "<p>Halo,</p>" +
                            "<p>Berikut kode OTP untuk reset password akun BEKAL Anda:</p>" +
                            "<h2 style=\"letter-spacing:4px\">" + otp + "</h2>" +
                            "<p>Kode ini berlaku selama <b>10 menit</b>. Jangan bagikan kode ini kepada siapa pun.</p>" +
                            "<p>Jika Anda tidak meminta reset password ini, abaikan email ini.</p>" +
                            "</div>",
                    true
            );

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Gagal mengirim email OTP", e);
        }
    }
}
