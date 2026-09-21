package org.edwin.bekal.domain.application.service.impl;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.SendResponse;
import org.edwin.bekal.domain.application.entity.DeviceToken;
import org.edwin.bekal.domain.application.repository.DeviceTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceImplTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private BatchResponse batchResponse;

    @InjectMocks
    private PushNotificationServiceImpl pushNotificationService;

    @Nested
    @DisplayName("sendToCustomer Tests")
    class SendToCustomerTests {

        @Test
        @DisplayName("Should skip notification when customer has no registered device tokens")
        void sendToCustomer_noTokens_skipsNotification() {
            UUID customerId = UUID.randomUUID();
            given(deviceTokenRepository.findByCustomer_Id(customerId)).willReturn(List.of());

            pushNotificationService.sendToCustomer(customerId, "Title", "Body");

            verify(deviceTokenRepository).findByCustomer_Id(customerId);
            verifyNoMoreInteractions(deviceTokenRepository);
        }

        @Test
        @DisplayName("Should send FCM successfully when customer has tokens")
        void sendToCustomer_success() throws FirebaseMessagingException {
            UUID customerId = UUID.randomUUID();
            DeviceToken token = new DeviceToken();
            token.setFcmToken("token-123");

            given(deviceTokenRepository.findByCustomer_Id(customerId)).willReturn(List.of(token));
            given(batchResponse.getSuccessCount()).willReturn(1);
            given(batchResponse.getFailureCount()).willReturn(0);
            given(firebaseMessaging.sendEachForMulticast(any())).willReturn(batchResponse);

            try (MockedStatic<FirebaseMessaging> staticFirebase = mockStatic(FirebaseMessaging.class)) {
                staticFirebase.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

                pushNotificationService.sendToCustomer(customerId, "Title", "Body");

                verify(firebaseMessaging).sendEachForMulticast(any());
                verify(deviceTokenRepository, never()).deleteByFcmToken(any());
            }
        }

        @Test
        @DisplayName("Should clean up invalid tokens when FCM returns failure responses")
        void sendToCustomer_withFailedTokens_deletesBadTokens() throws FirebaseMessagingException {
            UUID customerId = UUID.randomUUID();
            DeviceToken token1 = new DeviceToken();
            token1.setFcmToken("valid-token");

            DeviceToken token2 = new DeviceToken();
            token2.setFcmToken("invalid-token");

            given(deviceTokenRepository.findByCustomer_Id(customerId)).willReturn(List.of(token1, token2));

            SendResponse successResponse = mock(SendResponse.class);
            given(successResponse.isSuccessful()).willReturn(true);

            SendResponse failedResponse = mock(SendResponse.class);
            given(failedResponse.isSuccessful()).willReturn(false);

            given(batchResponse.getSuccessCount()).willReturn(1);
            given(batchResponse.getFailureCount()).willReturn(1);
            given(batchResponse.getResponses()).willReturn(List.of(successResponse, failedResponse));
            given(firebaseMessaging.sendEachForMulticast(any())).willReturn(batchResponse);

            try (MockedStatic<FirebaseMessaging> staticFirebase = mockStatic(FirebaseMessaging.class)) {
                staticFirebase.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

                pushNotificationService.sendToCustomer(customerId, "Title", "Body");

                verify(deviceTokenRepository).deleteByFcmToken("invalid-token");
                verify(deviceTokenRepository, never()).deleteByFcmToken("valid-token");
            }
        }

        @Test
        @DisplayName("Should handle FirebaseMessagingException gracefully without throwing")
        void sendToCustomer_firebaseException_handledGracefully() throws FirebaseMessagingException {
            UUID customerId = UUID.randomUUID();
            DeviceToken token = new DeviceToken();
            token.setFcmToken("token-123");

            given(deviceTokenRepository.findByCustomer_Id(customerId)).willReturn(List.of(token));

            FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
            given(firebaseMessaging.sendEachForMulticast(any())).willThrow(exception);

            try (MockedStatic<FirebaseMessaging> staticFirebase = mockStatic(FirebaseMessaging.class)) {
                staticFirebase.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

                pushNotificationService.sendToCustomer(customerId, "Title", "Body");

                verify(firebaseMessaging).sendEachForMulticast(any());
            }
        }
    }
}