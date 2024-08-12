package woozlabs.echo.domain.gmail.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.gmail.dto.pubsub.FcmTokenResponse;
import woozlabs.echo.domain.gmail.dto.pubsub.MessageInHistoryData;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubMessage;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubNotification;
import woozlabs.echo.domain.gmail.entity.PubSubHistory;
import woozlabs.echo.domain.gmail.repository.PubSubHistoryRepository;
import woozlabs.echo.domain.gmail.validator.PubSubValidator;
import woozlabs.echo.domain.gmail.entity.FcmToken;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.gmail.repository.FcmTokenRepository;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static woozlabs.echo.global.constant.GlobalConstant.*;

@Service
@RequiredArgsConstructor
public class PubSubService {
    private final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/gmail.modify",
            "https://mail.google.com/"

    );
    private final Long MAX_HISTORY_COUNT = 50L;
    private final String PUB_SUB_LABEL_ID = "INBOX";
    private final List<String> PUB_SUB_HISTORY_TYPE = List.of("messageAdded");
    // injection & init
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final ObjectMapper om;
    private final MemberRepository memberRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final PubSubHistoryRepository pubSubHistoryRepository;
    private final PubSubValidator pubSubValidator;

    @Transactional
    public void handleFirebaseCloudMessage(PubSubMessage pubsubMessage) throws Exception {
        String messageData = pubsubMessage.getMessage().getData();
        String decodedData = new String(java.util.Base64.getDecoder().decode(messageData));
        PubSubNotification notification = om.readValue(decodedData, PubSubNotification.class);
        String email = notification.getEmailAddress();
        BigInteger newHistoryId = new BigInteger(notification.getHistoryId());
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE)
        );
        PubSubHistory pubSubHistory = pubSubHistoryRepository.findByMember(member).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_PUB_SUB_HISTORY_ERR)
        );
        Gmail gmailService = createGmailService(member.getAccessToken());
        List<FcmToken> fcmTokens = fcmTokenRepository.findByMember(member);
        List<MessageInHistoryData> getHistoryList = getHistoryListById(pubSubHistory, newHistoryId, gmailService);
        if(getHistoryList.isEmpty()) return; // watch message
        fcmTokens.forEach((fcmToken) -> {
            String token = fcmToken.getFcmToken();
            getHistoryList.forEach((historyData) -> {
                // handle fcm
                Message message = Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle(notification.getEmailAddress())
                                .setBody(historyData.getId())
                                .build()
                        )
                        .setToken(token)
                        .build();
                try{
                    String response = FirebaseMessaging.getInstance().send(message);
                    System.out.println(response);
                } catch (FirebaseMessagingException e) {
                    throw new CustomErrorException(ErrorCode.FIREBASE_CLOUD_MESSAGING_SEND_ERR);
                }
            });
        });
    }

    @Transactional
    public FcmTokenResponse saveFcmToken(String uid, String fcmToken){
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        List<FcmToken> tokens = fcmTokenRepository.findByMember(member);
        pubSubValidator.validateSaveFcmToken(tokens, fcmToken);
        FcmToken newFcmToken = FcmToken.builder()
                .fcmToken(fcmToken)
                .member(member).build();
        fcmTokenRepository.save(newFcmToken);
        return new FcmTokenResponse(newFcmToken.getId());
    }

    private List<MessageInHistoryData> getHistoryListById(PubSubHistory pubSubHistory, BigInteger newHistoryId, Gmail gmailService) throws IOException {
        BigInteger historyId = pubSubHistory.getHistoryId();
        ListHistoryResponse historyResponse = gmailService.users().history()
                .list(USER_ID)
                .setStartHistoryId(historyId)
                .setLabelId(PUB_SUB_LABEL_ID)
                .setHistoryTypes(PUB_SUB_HISTORY_TYPE)
                .setMaxResults(MAX_HISTORY_COUNT)
                .execute();
        List<History> histories = historyResponse.getHistory();
        List<MessageInHistoryData> historyDataList = new ArrayList<>();
        if(histories == null) return historyDataList;
        for(History history : histories){
            List<HistoryMessageAdded> addedMessages = history.getMessagesAdded();
            addedMessages.forEach((addedMessage) -> {
                com.google.api.services.gmail.model.Message message = addedMessage.getMessage();
                historyDataList.add(MessageInHistoryData.toMessageInHistoryData(message));
            });
        }
        pubSubHistory.updateHistoryId(newHistoryId);
        return historyDataList;
    }

    private Gmail createGmailService(String accessToken) throws Exception{
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        HttpRequestInitializer requestInitializer = createCredentialWithAccessToken(accessToken);
        return new Gmail.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName("Echo")
                .build();
    }

    private HttpRequestInitializer createCredentialWithAccessToken(String accessToken){
        AccessToken token = AccessToken.newBuilder()
                .setTokenValue(accessToken)
                .setScopes(SCOPES)
                .build();
        GoogleCredentials googleCredentials = GoogleCredentials.create(token);
        return new HttpCredentialsAdapter(googleCredentials);
    }
}