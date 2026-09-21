package org.edwin.bekal.domain.application.service.impl;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edwin.bekal.domain.application.repository.DeviceTokenRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationServiceImpl {

    private final DeviceTokenRepository deviceTokenRepository;

    public void sendToCustomer(UUID customerId, String title, String body) {
        List<String> tokens = deviceTokenRepository.findByCustomer_Id(customerId)
                .stream()
                .map(dt -> dt.getFcmToken())
                .toList();

        if (tokens.isEmpty()) {
            log.warn("Customer {} tidak punya device token terdaftar, skip notifikasi", customerId);
            return;
        }

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("type", "LOAN_DISBURSED")   // data tambahan, dipakai Android buat routing saat notif diklik
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("FCM sent: {} success, {} failed", response.getSuccessCount(), response.getFailureCount());

            // Cleanup token yang sudah invalid (uninstall app, dsb)
            if (response.getFailureCount() > 0) {
                for (int i = 0; i < response.getResponses().size(); i++) {
                    if (!response.getResponses().get(i).isSuccessful()) {
                        String badToken = tokens.get(i);
                        deviceTokenRepository.deleteByFcmToken(badToken);
                        log.info("Token invalid dihapus: {}", badToken);
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Gagal kirim FCM ke customer {}", customerId, e);
        }
    }
}
