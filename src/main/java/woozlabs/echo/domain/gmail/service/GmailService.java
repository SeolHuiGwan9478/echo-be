package woozlabs.echo.domain.gmail.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Thread;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import woozlabs.echo.domain.gmail.dto.autoForwarding.AutoForwardingResponse;
import woozlabs.echo.domain.gmail.dto.draft.*;
import woozlabs.echo.domain.gmail.dto.history.*;
import woozlabs.echo.domain.gmail.dto.message.*;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessagesResponse;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadTotalCountResponse;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubWatchRequest;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubWatchResponse;
import woozlabs.echo.domain.gmail.dto.thread.*;
import woozlabs.echo.domain.gmail.entity.FcmToken;
import woozlabs.echo.domain.gmail.entity.PubSubHistory;
import woozlabs.echo.domain.gmail.exception.GmailException;
import woozlabs.echo.domain.gmail.repository.FcmTokenRepository;
import woozlabs.echo.domain.gmail.repository.PubSubHistoryRepository;
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.domain.gmail.validator.PubSubValidator;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;
import woozlabs.echo.global.utils.GlobalUtility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static woozlabs.echo.global.constant.GlobalConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GmailService {
    // constants
    private final String TEMP_FILE_PREFIX = "echo";
    // injection & init
    private final MultiThreadGmailService multiThreadGmailService;
    private final AccountRepository accountRepository;
    private final PubSubHistoryRepository pubSubHistoryRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final GmailUtility gmailUtility;
    private final PubSubValidator pubSubValidator;

    public GmailThreadListResponse getQueryUserEmailThreads(String uid, String pageToken, Long maxResults, String q) {
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        // ---- temp data ----
        LocalDate currentDate = LocalDate.now();
        Boolean isBilling = Boolean.FALSE;
        // -------------------
        ListThreadsResponse response = getQueryListThreadsResponse(pageToken, maxResults, q, gmailService);
        List<Thread> threads = response.getThreads(); // get threads
        threads = isEmptyResult(threads);
        List<GmailThreadListThreads> detailedThreads = getDetailedThreads(threads, gmailService); // get detailed threads
        if(pageToken != null){
            validatePayment(detailedThreads, currentDate);
        }
        return GmailThreadListResponse.builder()
                .threads(detailedThreads)
                .nextPageToken(response.getNextPageToken())
                .build();
    }

    private void validatePayment(List<GmailThreadListThreads> detailedThreads, LocalDate currentDate) {
        if(!detailedThreads.isEmpty()){
            // get first thread date
            GmailThreadListThreads firstThread = detailedThreads.get(0);
            GmailThreadGetMessagesResponse lastMessageInFirstThread = firstThread.getMessages().get(0);
            Long timeStamp = lastMessageInFirstThread.getTimestamp();
            String timeZone = lastMessageInFirstThread.getTimezone();
            Instant instant = Instant.ofEpochMilli(timeStamp);
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of(timeZone));
            LocalDate firstThreadDate = zonedDateTime.toLocalDate();
            // check validation
            LocalDate beforeSixtyDays = currentDate.minusDays(60);
            if(firstThreadDate.isBefore(beforeSixtyDays)){
                throw new CustomErrorException(ErrorCode.BILLING_ERROR_MESSAGE, ErrorCode.BILLING_ERROR_MESSAGE.getMessage());
            }
        }
    }

    public GmailThreadGetResponse getUserEmailThread(String uid, String id){
        try {
            Account account = accountRepository.findByUid(uid).orElseThrow(
                    () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
            String accessToken = account.getAccessToken();
            Gmail gmailService = gmailUtility.createGmailService(accessToken);
            GmailThreadGetResponse gmailThreadGetResponse = new GmailThreadGetResponse();
            Thread thread = getOneThreadResponse(id, gmailService);
            List<Message> messages = thread.getMessages();
            List<GmailThreadGetMessagesFrom> froms = new ArrayList<>();
            List<GmailThreadGetMessagesCc> ccs = new ArrayList<>();
            List<GmailThreadGetMessagesBcc> bccs = new ArrayList<>();
            Map<String, GmailThreadListAttachments> attachments = new HashMap<>();
            Map<String, GmailThreadListInlineImages> inlineImages = new HashMap<>();
            List<GmailThreadGetMessagesResponse> convertedMessages = new ArrayList<>();
            List<String> labelIds = new ArrayList<>();
            for (int idx = 0; idx < messages.size(); idx++) {
                int idxForLambda = idx;
                Message message = messages.get(idx);
                MessagePart payload = message.getPayload();
                convertedMessages.add(GmailThreadGetMessagesResponse.toGmailThreadGetMessages(message));
                List<MessagePartHeader> headers = payload.getHeaders(); // parsing header
                labelIds.addAll(message.getLabelIds());
                if (idxForLambda == messages.size() - 1) {
                    Long date = convertedMessages.get(convertedMessages.size() - 1).getTimestamp();
                    gmailThreadGetResponse.setSnippet(message.getSnippet());
                    gmailThreadGetResponse.setTimestamp(date);
                }
                // get attachments
                getThreadsAttachments(payload, attachments, inlineImages);
                headers.forEach((header) -> {
                    String headerName = header.getName().toUpperCase();
                    // first message -> extraction subject
                    if (idxForLambda == 0 && headerName.equals(THREAD_PAYLOAD_HEADER_SUBJECT_KEY)) {
                        gmailThreadGetResponse.setSubject(header.getValue());
                    }
                });
                GmailThreadGetMessagesResponse gmailThreadGetMessage = convertedMessages.get(convertedMessages.size() - 1);
                froms.add(gmailThreadGetMessage.getFrom());
                ccs.addAll(gmailThreadGetMessage.getCc());
                bccs.addAll(gmailThreadGetMessage.getBcc());
            }
            gmailThreadGetResponse.setLabelIds(labelIds.stream().distinct().collect(Collectors.toList()));
            gmailThreadGetResponse.setId(id);
            gmailThreadGetResponse.setHistoryId(thread.getHistoryId());
            gmailThreadGetResponse.setFrom(froms.stream().distinct().toList());
            gmailThreadGetResponse.setCc(ccs.stream().distinct().toList());
            gmailThreadGetResponse.setBcc(bccs.stream().distinct().toList());
            gmailThreadGetResponse.setThreadSize(messages.size());
            gmailThreadGetResponse.setAttachments(attachments);
            gmailThreadGetResponse.setAttachmentSize(attachments.size());
            gmailThreadGetResponse.setInlineImages(inlineImages);
            gmailThreadGetResponse.setInlineImageSize(inlineImages.size());
            gmailThreadGetResponse.setMessages(convertedMessages);
            return gmailThreadGetResponse;
        }catch (IOException e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST.getMessage());
        }
    }

    public GmailThreadTrashResponse trashUserEmailThread(String uid, String id) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        Thread trashedThread = gmailService.users().threads().trash(USER_ID, id)
                .setPrettyPrint(Boolean.TRUE)
                .execute();
        return new GmailThreadTrashResponse(trashedThread.getId());
    }

    public GmailThreadDeleteResponse deleteUserEmailThread(String uid, String id) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        gmailService.users().threads().delete(USER_ID, id)
                .setPrettyPrint(Boolean.TRUE)
                .execute();
        return new GmailThreadDeleteResponse(id);
    }

    public GmailThreadSearchListResponse searchUserEmailThreads(String uid, GmailSearchParams params) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        ListThreadsResponse response = getSearchListThreadsResponse(params, gmailService);
        List<Thread> threads = response.getThreads();
        threads = isEmptyResult(threads);
        List<GmailThreadSearchListThreads> searchedThreads = getSimpleThreads(threads); // get detailed threads
        return GmailThreadSearchListResponse.builder()
                .threads(searchedThreads)
                .nextPageToken(response.getNextPageToken())
                .build();
    }

    public GmailMessageGetResponse getUserEmailMessage(String uid, String messageId) throws Exception {
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        Message message = gmailService.users().messages().get(USER_ID, messageId).execute();
        return GmailMessageGetResponse.toGmailMessageGet(message, gmailUtility);
    }

    public GmailMessageGetResponse getUserEmailMessageWithoutVerification(String uid, String messageId) throws Exception {
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        Message message = gmailService.users().messages().get(USER_ID, messageId).execute();
        return GmailMessageGetResponse.toGmailMessageGet(message, gmailUtility);
    }

    public GmailMessageAttachmentResponse getAttachment(String uid, String messageId, String id) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        MessagePartBody attachment = gmailService.users().messages()
                .attachments()
                .get(USER_ID, messageId, id)
                .execute();
        String standardBase64 = attachment.getData()
                .replace('-', '+')
                .replace('_', '/');
        // Add padding if necessary
        int paddingCount = (4 - (standardBase64.length() % 4)) % 4;
        for (int i = 0; i < paddingCount; i++) {
            standardBase64 += "=";
        }
        byte[] decodedBinaryContent = java.util.Base64.getDecoder().decode(standardBase64);
        //byte[] attachmentData = java.util.Base64.getDecoder().decode(attachment.getData());
        String standardData = java.util.Base64.getEncoder().encodeToString(decodedBinaryContent);
        return GmailMessageAttachmentResponse.builder()
                .attachmentId(attachment.getAttachmentId())
                .size(attachment.getSize())
                .data(standardData)
                .build();
    }

    public GmailMessageSendResponse sendUserEmailMessage(String uid, GmailMessageSendRequest request) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        Profile profile = gmailService.users().getProfile(USER_ID).execute();
        String fromEmailAddress = profile.getEmailAddress();
        request.setFromEmailAddress(fromEmailAddress);
        MimeMessage mimeMessage = createEmail(request);
        Message message = createMessage(mimeMessage);
        Message responseMessage = gmailService.users().messages().send(USER_ID, message).execute();
        return GmailMessageSendResponse.builder()
                .id(responseMessage.getId())
                .threadId(responseMessage.getThreadId())
                .labelsId(responseMessage.getLabelIds())
                .snippet(responseMessage.getSnippet()).build();
    }

    public GmailThreadTotalCountResponse getUserEmailThreadsTotalCount(String uid, String label) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        int totalCount = getTotalCountThreads(gmailService, label);
        return GmailThreadTotalCountResponse.builder()
                .totalCount(totalCount)
                .build();
    }

    public GmailDraftSendResponse sendUserEmailDraft(String uid, GmailDraftCommonRequest request) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        Profile profile = gmailService.users().getProfile(USER_ID).execute();
        String fromEmailAddress = profile.getEmailAddress();
        request.setFromEmailAddress(fromEmailAddress);
        MimeMessage mimeMessage = createDraft(request);
        Message message = createMessage(mimeMessage);
        // create draft
        Draft draft = new Draft();
        draft.setMessage(message);
        Message responseMessage = gmailService.users().drafts().send(USER_ID, draft).execute();
        return GmailDraftSendResponse.builder()
                .id(responseMessage.getId())
                .threadId(responseMessage.getThreadId())
                .labelsId(responseMessage.getLabelIds())
                .snippet(responseMessage.getSnippet()).build();
    }

    public GmailDraftGetResponse getUserEmailDraft(String uid, String id) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        Draft draft = getOneDraftResponse(id, gmailService);
        GmailDraftGetMessage message = GmailDraftGetMessage.toGmailDraftGetMessages(draft.getMessage());
        return GmailDraftGetResponse.builder()
                .id(draft.getId())
                .message(message)
                .build();
    }

    public GmailDraftUpdateResponse updateUserEmailDraft(String uid, String id, GmailDraftCommonRequest request) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        Profile profile = gmailService.users().getProfile(USER_ID).execute();
        String fromEmailAddress = profile.getEmailAddress();
        request.setFromEmailAddress(fromEmailAddress);
        MimeMessage mimeMessage = createDraft(request);
        Message message = createMessage(mimeMessage);
        // create new draft
        Draft draft = new Draft().setMessage(message);
        draft = gmailService.users().drafts().update(USER_ID, id, draft).execute();
        GmailDraftGetMessage changedMessage = GmailDraftGetMessage.toGmailDraftGetMessages(draft.getMessage());
        return GmailDraftUpdateResponse.builder()
                .id(draft.getId())
                .message(changedMessage)
                .build();
    }

    public GmailDraftCreateResponse createUserEmailDraft(String uid, GmailDraftCommonRequest request) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        Profile profile = gmailService.users().getProfile(USER_ID).execute();
        String fromEmailAddress = profile.getEmailAddress();
        request.setFromEmailAddress(fromEmailAddress);
        MimeMessage mimeMessage = createDraft(request);
        Message message = createMessage(mimeMessage);
        // create new draft
        Draft draft = new Draft().setMessage(message);
        draft = gmailService.users().drafts().create(USER_ID, draft).execute();
        //GmailDraftGetMessage changedMessage = GmailDraftGetMessage.toGmailDraftGetMessages(draft.getMessage());
        return GmailDraftCreateResponse.builder()
                .id(draft.getId())
                //.message(changedMessage)
                .build();
    }

    public GmailThreadUpdateResponse updateUserEmailThread(String uid, String id, GmailThreadUpdateRequest request) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        ModifyThreadRequest modifyThreadRequest = new ModifyThreadRequest();
        modifyThreadRequest.setAddLabelIds(request.getAddLabelIds());
        modifyThreadRequest.setRemoveLabelIds(request.getRemoveLabelIds());
        gmailService.users().threads().modify(USER_ID, id, modifyThreadRequest).execute();
        return GmailThreadUpdateResponse.builder()
                .addLabelIds(request.getAddLabelIds())
                .removeLabelIds(request.getRemoveLabelIds())
                .build();
    }

    public GmailMessageUpdateResponse updateUserEmailMessage(String uid, String id, GmailMessageUpdateRequest request) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        ModifyMessageRequest modifyMessageRequest = new ModifyMessageRequest();
        modifyMessageRequest.setAddLabelIds(request.getAddLabelIds());
        modifyMessageRequest.setRemoveLabelIds(request.getRemoveLabelIds());
        gmailService.users().messages().modify(USER_ID, id, modifyMessageRequest).execute();
        return GmailMessageUpdateResponse.builder()
                .addLabelIds(request.getAddLabelIds())
                .removeLabelIds(request.getRemoveLabelIds())
                .build();
    }

    @Transactional
    public PubSubWatchResponse subscribePubSub(String uid, PubSubWatchRequest dto) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        List<FcmToken> fcmTokens = fcmTokenRepository.findByAccount(account);
        pubSubValidator.validateWatch(fcmTokens);
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        WatchRequest watchRequest = new WatchRequest()
                .setLabelIds(dto.getLabelIds())
                .setLabelFilterBehavior("include")
                .setTopicName("projects/echo-email-app/topics/gmail");
        WatchResponse watchResponse = gmailService.users().watch(USER_ID, watchRequest).execute();
        Optional<PubSubHistory> pubSubHistory = pubSubHistoryRepository.findByAccount(account);
        if(pubSubHistory.isEmpty()){
            PubSubHistory newHistory = PubSubHistory.builder()
                    .historyId(watchResponse.getHistoryId())
                    .account(account).build();
            pubSubHistoryRepository.save(newHistory);
        }else{
            PubSubHistory findHistory = pubSubHistory.get();
            findHistory.updateHistoryId(watchResponse.getHistoryId());
        }
        return PubSubWatchResponse.builder()
                .historyId(watchResponse.getHistoryId())
                .expiration(watchResponse.getExpiration()).build();
    }

    public void stopPubSub(String uid) throws Exception {
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE
                        , ErrorCode.NOT_FOUND_ACCESS_TOKEN.getMessage())
        );
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        gmailService.users().stop(USER_ID).execute();
    }

    public GmailHistoryListResponse getHistories(String uid, String historyId, String pageToken) throws Exception {
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        ListHistoryResponse historyResponse = gmailService
                .users()
                .history()
                .list(USER_ID)
                .setLabelId(HISTORY_INBOX_LABEL)
                .setPageToken(pageToken)
                .setStartHistoryId(new BigInteger(historyId))
                .execute();
        List<History> histories = historyResponse.getHistory(); // get histories
        GmailHistoryListResponse response = GmailHistoryListResponse.builder()
                .nextPageToken(historyResponse.getNextPageToken())
                .historyId(historyResponse.getHistoryId())
                .build();
        if(histories == null) return response;
        // convert history format
        List<GmailHistoryListData> historyListData = histories.stream().map((history) -> {
            List<GmailHistoryListMessageAdded> messagesAdded = history.getMessagesAdded() != null
                    ? history.getMessagesAdded().stream()
                    .map(GmailHistoryListMessageAdded::toGmailHistoryListMessageAdded)
                    .toList()
                    : Collections.emptyList();

            List<GmailHistoryListMessageDeleted> messagesDeleted = history.getMessagesDeleted() != null
                    ? history.getMessagesDeleted().stream()
                    .map(GmailHistoryListMessageDeleted::toGmailHistoryListMessageDeleted)
                    .toList()
                    : Collections.emptyList();

            List<GmailHistoryListLabelAdded> labelsAdded = history.getLabelsAdded() != null
                    ? history.getLabelsAdded().stream()
                    .map(GmailHistoryListLabelAdded::toGmailHistoryListLabelAdded)
                    .toList()
                    : Collections.emptyList();

            List<GmailHistoryListLabelRemoved> labelsRemoved = history.getLabelsRemoved() != null
                    ? history.getLabelsRemoved().stream()
                    .map(GmailHistoryListLabelRemoved::toGmailHistoryListLabelRemoved)
                    .toList()
                    : Collections.emptyList();
            return GmailHistoryListData.builder()
                    .messagesAdded(messagesAdded)
                    .messagesDeleted(messagesDeleted)
                    .labelsAdded(labelsAdded)
                    .labelsRemoved(labelsRemoved)
                    .build();
        }).toList();
        response.setHistory(historyListData);
        return response;
    }

    public AutoForwardingResponse setUpAutoForwarding(String uid, String q, String email) throws IOException {
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        addForwardingAddress(email, gmailService);
        createFilter(q, email, gmailService);
        return AutoForwardingResponse.builder()
                .q(q)
                .forwardingEmail(email)
                .build();
    }

    public void generateVerificationLabel(String uid) throws IOException {
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        // find echo verification label
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        ListLabelsResponse listLabelsResponse = gmailService.users().labels().list(USER_ID).execute();
        for(Label label : listLabelsResponse.getLabels()){
            if(label.getName().equals(PARENT_VERIFICATION_LABEL + "/" + CHILD_VERIFICATION_LABEL)){
                return;
            }
        }
        // create echo verification label
        Label parentLabel = new Label()
                .setName(PARENT_VERIFICATION_LABEL)
                .setLabelListVisibility("labelShow")
                .setMessageListVisibility("show");
        gmailService.users().labels().create(USER_ID, parentLabel).execute();
        Label childLabel = new Label()
                .setName(PARENT_VERIFICATION_LABEL + "/" + CHILD_VERIFICATION_LABEL)
                .setLabelListVisibility("labelShow")
                .setMessageListVisibility("show");
        gmailService.users().labels().create(USER_ID, childLabel).execute();
    }

    public GmailMessageAttachmentDownloadResponse downloadAttachment(String uid, String messageId, String attachmentId) throws Exception {
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        MessagePartBody attachPart = gmailService
                .users()
                .messages()
                .attachments()
                .get(USER_ID, messageId, attachmentId).execute();
        String standardBase64 = attachPart.getData()
                .replace('-', '+')
                .replace('_', '/');
        // Add padding if necessary
        int paddingCount = (4 - (standardBase64.length() % 4)) % 4;
        for (int i = 0; i < paddingCount; i++) {
            standardBase64 += "=";
        }
        byte[] decodedBinaryContent = java.util.Base64.getDecoder().decode(standardBase64);
        ByteArrayResource resource = new ByteArrayResource(decodedBinaryContent);
        return GmailMessageAttachmentDownloadResponse.builder()
                .attachmentId(attachmentId)
                .size(attachPart.getSize())
                .byteData(decodedBinaryContent)
                .resource(resource)
                .build();
    }

    public void getGoogleDriveFileId(String uid, String messageId) throws IOException {
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        // Gmail API를 사용하여 메시지를 가져옴
        Message message = gmailService.users().messages().get(USER_ID, messageId).execute();

        List<String> fileIds = new ArrayList<>();

        // 메시지의 페이로드 부분에서 MIME 타입이 "text/html"인 부분을 찾음
        for (MessagePart part : message.getPayload().getParts()) {
            if ("text/html".equals(part.getMimeType())) {
                String standardBase64 = part.getBody().getData()
                        .replace('-', '+')
                        .replace('_', '/');
                // Add padding if necessary
                int paddingCount = (4 - (standardBase64.length() % 4)) % 4;
                for (int i = 0; i < paddingCount; i++) {
                    standardBase64 += "=";
                }
                byte[] decodedBinaryContent = java.util.Base64.getDecoder().decode(standardBase64);
                String decodedData = new String(decodedBinaryContent, "UTF-8");
                System.out.println(decodedData);

                // Google Drive 파일 URL 패턴을 추출함
                Pattern pattern = Pattern.compile("https://docs\\.google\\.com/document/d/([a-zA-Z0-9-_]+)");
                Matcher matcher = pattern.matcher(decodedData);

                // 모든 매칭되는 파일 ID를 리스트에 추가
                while (matcher.find()) {
                    fileIds.add(matcher.group(1));
                }
            }
        }
        System.out.println(fileIds);
    }

    // Methods : get something
    private List<GmailThreadListThreads> getDetailedThreads(List<Thread> threads, Gmail gmailService) {
        //int nThreads = Runtime.getRuntime().availableProcessors();
        int nThreads = 25;
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        List<CompletableFuture<GmailThreadListThreads>> futures = threads.stream()
                .map((thread) -> {
                    CompletableFuture<GmailThreadListThreads> future = new CompletableFuture<>();
                    executor.execute(() -> {
                        try{
                            GmailThreadListThreads result = multiThreadGmailService
                                    .multiThreadRequestGmailThreadGetForList(thread, gmailService);
                            future.complete(result);
                        }catch (Exception e){
                            log.error(REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG);
                            future.completeExceptionally(new GmailException(REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG));
                        }
                    });
                    return future;
                }).toList();
        return futures.stream().map((future) -> {
            try{
                return future.get();
            }catch (Exception e){
                log.error(e.getMessage());
                throw new GmailException(REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG);
            }
        }).collect(Collectors.toList());
    }

    private List<GmailThreadSearchListThreads> getSimpleThreads(List<Thread> threads){
        List<GmailThreadSearchListThreads> gmailThreadSearchListThreads = new ArrayList<>();
        threads.forEach((thread) ->{
            GmailThreadSearchListThreads gmailThreadSearchListThread = new GmailThreadSearchListThreads();
            gmailThreadSearchListThread.setId(thread.getId());
            gmailThreadSearchListThreads.add(gmailThreadSearchListThread);
        });
        return gmailThreadSearchListThreads;
    }

    private ListThreadsResponse getQueryListThreadsResponse(String pageToken, Long maxResults, String q, Gmail gmailService) {
        try{
            return gmailService.users().threads()
                    .list(USER_ID)
                    .setMaxResults(maxResults)
                    .setPageToken(pageToken)
                    .setPrettyPrint(Boolean.TRUE)
                    .setQ(q)
                    .execute();
        }catch (GoogleJsonResponseException e){
            e.printStackTrace();
            switch (e.getStatusCode()) {
                case 401 ->
                        throw new CustomErrorException(ErrorCode.INVALID_ACCESS_TOKEN, ErrorCode.INVALID_ACCESS_TOKEN.getMessage());
                case 429 ->
                        throw new CustomErrorException(ErrorCode.TOO_MANY_REQUESTS, ErrorCode.TOO_MANY_REQUESTS.getMessage());
                case 400 -> {
                    if (e.getDetails().getMessage().contains("Invalid pageToken")) {
                        throw new CustomErrorException(ErrorCode.INVALID_NEXT_PAGE_TOKEN, ErrorCode.INVALID_NEXT_PAGE_TOKEN.getMessage());
                    }
                    throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE.getMessage());
                }
                default ->
                        throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE.getMessage());
            }
        }catch (IOException e){
            e.printStackTrace();
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE.getMessage());
        }
    }

    private ListThreadsResponse getSearchListThreadsResponse(GmailSearchParams params, Gmail gmailService) throws IOException{
        String q = params.createQ();
        return gmailService.users().threads()
                .list(USER_ID)
                .setMaxResults(THREADS_LIST_MAX_LENGTH)
                .setPrettyPrint(Boolean.TRUE)
                .setQ(q)
                .execute();
    }

    private Thread getOneThreadResponse(String id, Gmail gmailService) throws IOException {
        return gmailService.users().threads()
                .get(USER_ID, id)
                .setFormat(THREADS_GET_FULL_FORMAT)
                .setPrettyPrint(Boolean.TRUE)
                .execute();
    }

    private ListDraftsResponse getListDraftsResponse(Gmail gmailService, String pageToken, String q) throws IOException{
        return gmailService.users().drafts()
                .list(USER_ID)
                .setMaxResults(THREADS_LIST_MAX_LENGTH)
                .setPrettyPrint(Boolean.TRUE)
                .setPageToken(pageToken)
                .setQ(q)
                .execute();
    }

    private Draft getOneDraftResponse(String id, Gmail gmailService) throws IOException{
        return gmailService.users().drafts()
                .get(USER_ID, id)
                .setFormat(DRAFTS_GET_FULL_FORMAT)
                .setPrettyPrint(Boolean.TRUE)
                .execute();
    }

    // Methods : create something

    private MimeMessage createEmail(GmailMessageSendRequest request) throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        // setting base
        email.setFrom(new InternetAddress(request.getFromEmailAddress()));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO,
                new InternetAddress(request.getToEmailAddress()));
        email.setSubject(request.getSubject());
        // setting body
        Multipart multipart = new MimeMultipart();
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(request.getBodyText(), MULTI_PART_TEXT_PLAIN);
        multipart.addBodyPart(mimeBodyPart); // set bodyText

        for(MultipartFile mimFile : request.getFiles()){
            MimeBodyPart fileMimeBodyPart = new MimeBodyPart();
            if(mimFile.getOriginalFilename() == null) throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_MESSAGES_SEND_API_ERROR_MESSAGE);
            File file = File.createTempFile(TEMP_FILE_PREFIX, mimFile.getOriginalFilename());
            mimFile.transferTo(file);
            DataSource source = new FileDataSource(file);
            fileMimeBodyPart.setFileName(mimFile.getOriginalFilename());
            fileMimeBodyPart.setDataHandler(new DataHandler(source));
            multipart.addBodyPart(fileMimeBodyPart);
            file.deleteOnExit();
        }
        email.setContent(multipart);
        return email;
    }

    private MimeMessage createDraft(GmailDraftCommonRequest request) throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        // setting base
        email.setFrom(new InternetAddress(request.getFromEmailAddress()));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO,
                new InternetAddress(request.getToEmailAddress()));
        email.setSubject(request.getSubject());
        // setting body
        Multipart multipart = new MimeMultipart();
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(request.getBodyText(), MULTI_PART_TEXT_PLAIN);
        multipart.addBodyPart(mimeBodyPart); // set bodyText
        List<MultipartFile> files = request.getFiles() != null ? request.getFiles() : new ArrayList<>();
        for(MultipartFile mimFile : files){
            MimeBodyPart fileMimeBodyPart = new MimeBodyPart();
            if(mimFile.getOriginalFilename() == null) throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_DRAFTS_SEND_API_ERROR_MESSAGE);
            File file = File.createTempFile(TEMP_FILE_PREFIX, mimFile.getOriginalFilename());
            mimFile.transferTo(file);
            DataSource source = new FileDataSource(file);
            fileMimeBodyPart.setFileName(mimFile.getOriginalFilename());
            fileMimeBodyPart.setDataHandler(new DataHandler(source));
            multipart.addBodyPart(fileMimeBodyPart);
            file.deleteOnExit();
        }
        email.setContent(multipart);
        return email;
    }


    private Message createMessage(MimeMessage emailContent) throws MessagingException, IOException{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    private <T> List<T> isEmptyResult(List<T> list){
        if(list == null) return new ArrayList<>();
        return list;
    }

    private int getTotalCountThreads(Gmail gmailService, String label) throws IOException {
        Label result = gmailService.users().labels()
                .get(USER_ID, label)
                .execute();
        return result.getThreadsTotal();
    }

    private void getThreadsAttachments(MessagePart part, Map<String, GmailThreadListAttachments> attachments, Map<String, GmailThreadListInlineImages> inlineImages) throws IOException {
        if(part.getParts() == null){ // base condition
            if(part.getFilename() != null && !part.getFilename().isBlank() && !GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                List<MessagePartHeader> headers = part.getHeaders();
                GmailThreadListAttachments attachment = GmailThreadListAttachments.builder().build();
                String contentId = "";
                for(MessagePartHeader header : headers){
                    if(header.getName().toUpperCase().equals(THREAD_PAYLOAD_HEADER_CONTENT_ID_KEY)){
                        contentId = header.getValue();
                        contentId = contentId.replace("<", "").replace(">", "");
                    }
                }
                attachment.setMimeType(part.getMimeType());
                attachment.setAttachmentId(body.getAttachmentId());
                attachment.setSize(body.getSize());
                attachment.setFileName(part.getFilename());
                if(!attachments.containsKey(contentId)){
                    attachments.put(contentId, attachment);
                }
            }else if(part.getFilename() != null && !part.getFilename().isBlank() && GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                List<MessagePartHeader> headers = part.getHeaders();
                GmailThreadListInlineImages inlineImage = GmailThreadListInlineImages.builder().build();
                String contentId = "";
                for(MessagePartHeader header : headers){
                    if(header.getName().toUpperCase().equals(THREAD_PAYLOAD_HEADER_CONTENT_ID_KEY)){
                        contentId = header.getValue();
                        contentId = contentId.replace("<", "").replace(">", "");
                    }
                }
                inlineImage.setMimeType(part.getMimeType());
                inlineImage.setAttachmentId(body.getAttachmentId());
                inlineImage.setSize(body.getSize());
                inlineImage.setFileName(part.getFilename());
                if(!inlineImages.containsKey(contentId)){
                    inlineImages.put(contentId, inlineImage);
                }
            }
        }else{ // recursion
            for(MessagePart subPart : part.getParts()){
                getThreadsAttachments(subPart, attachments, inlineImages);
            }
            if(part.getFilename() != null && !part.getFilename().isBlank() && !GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                List<MessagePartHeader> headers = part.getHeaders();
                GmailThreadListAttachments attachment = GmailThreadListAttachments.builder().build();
                String contentId = "";
                for(MessagePartHeader header : headers){
                    if(header.getName().toUpperCase().equals(THREAD_PAYLOAD_HEADER_CONTENT_ID_KEY)){
                        contentId = header.getValue();
                        contentId = contentId.replace("<", "").replace(">", "");
                    }
                }
                attachment.setMimeType(part.getMimeType());
                attachment.setAttachmentId(body.getAttachmentId());
                attachment.setSize(body.getSize());
                attachment.setFileName(part.getFilename());
                if(!attachments.containsKey(contentId)){
                    attachments.put(contentId, attachment);
                }
            }else if(part.getFilename() != null && !part.getFilename().isBlank() && GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                List<MessagePartHeader> headers = part.getHeaders();
                GmailThreadListInlineImages inlineImage = GmailThreadListInlineImages.builder().build();
                String contentId = "";
                for(MessagePartHeader header : headers){
                    if(header.getName().toUpperCase().equals(THREAD_PAYLOAD_HEADER_CONTENT_ID_KEY)){
                        contentId = header.getValue();
                        contentId = contentId.replace("<", "").replace(">", "");
                    }
                }
                inlineImage.setMimeType(part.getMimeType());
                inlineImage.setAttachmentId(body.getAttachmentId());
                inlineImage.setSize(body.getSize());
                inlineImage.setFileName(part.getFilename());
                if(!inlineImages.containsKey(contentId)){
                    inlineImages.put(contentId, inlineImage);
                }
            }
        }
    }

    private void addForwardingAddress(String forwardingEmailAddress, Gmail gmailService) throws IOException {
        ForwardingAddress forwardingAddress = new ForwardingAddress().setForwardingEmail(forwardingEmailAddress);
        gmailService.users().settings().forwardingAddresses().create(USER_ID, forwardingAddress).execute();
    }

    private void createFilter(String q, String forwardTo, Gmail gmailService) throws IOException {
        FilterCriteria filterCriteria = new FilterCriteria().setQuery(q);
        FilterAction filterAction = new FilterAction().setForward(forwardTo);
        Filter filter = new Filter().setCriteria(filterCriteria).setAction(filterAction);
        gmailService.users().settings().filters().create(USER_ID, filter).execute();
    }
}