package woozlabs.echo.domain.gmail.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.gmail.dto.pubsub.FcmTokenResponse;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubMessage;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubNotification;
import woozlabs.echo.domain.member.entity.FcmToken;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.FcmTokenRepository;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PubSubService {
    private final ObjectMapper om;
    private final MemberRepository memberRepository;
    private final FcmTokenRepository fcmTokenRepository;

    public void handleFirebaseCloudMessage(PubSubMessage pubsubMessage) throws JsonProcessingException {
        String messageData = pubsubMessage.getMessage().getData();
        String decodedData = new String(java.util.Base64.getDecoder().decode(messageData));
        PubSubNotification notification = om.readValue(decodedData, PubSubNotification.class);
        String email = notification.getEmailAddress();
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE)
        );
        List<FcmToken> fcmTokens = fcmTokenRepository.findByMember(member);
        fcmTokens.forEach((fcmToken) -> {
            String token = fcmToken.getFcmToken();
            // handle fcm
            com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(notification.getEmailAddress())
                            .setBody(notification.getHistoryId())
                            .build())
                    .setToken(token)
                    .build();
            try{
                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println(response);
            } catch (FirebaseMessagingException e) {
                throw new CustomErrorException(ErrorCode.FIREBASE_CLOUD_MESSAGING_SEND_ERR);
            }
        });

    }

    @Transactional
    public FcmTokenResponse saveFcmToken(String uid, String fcmToken){
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        FcmToken newFcmToken = FcmToken.builder()
                .fcmToken(fcmToken)
                .member(member).build();
        fcmTokenRepository.save(newFcmToken);
        return new FcmTokenResponse(newFcmToken.getId());
    }
}
