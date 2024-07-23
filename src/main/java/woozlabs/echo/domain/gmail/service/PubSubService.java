package woozlabs.echo.domain.gmail.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubMessage;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubNotification;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class PubSubService {
    private final ObjectMapper om;

    public void handleFirebaseCloudMessage(PubSubMessage pubsubMessage) throws JsonProcessingException {
        String messageData = pubsubMessage.getMessage().getData();
        String decodedData = new String(java.util.Base64.getDecoder().decode(messageData));
        String fcmToken = "test";
        PubSubNotification notification = om.readValue(decodedData, PubSubNotification.class);
        System.out.println(notification);
        // handle fcm
//        com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
//                .setNotification(Notification.builder()
//                        .setTitle(notification.getEmailAddress())
//                        .setBody(notification.getHistoryId())
//                        .build())
//                .setToken(fcmToken)
//                .build();
//        try{
//            String response = FirebaseMessaging.getInstance().send(message);
//            System.out.println(response);
//        } catch (FirebaseMessagingException e) {
//            throw new CustomErrorException(ErrorCode.FIREBASE_CLOUD_MESSAGING_SEND_ERR);
//        }
    }
}
