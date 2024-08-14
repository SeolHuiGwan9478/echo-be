package woozlabs.echo.domain.gmail.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Thread;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import woozlabs.echo.domain.gmail.dto.draft.*;
import woozlabs.echo.domain.gmail.dto.message.GmailMessageAttachmentResponse;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessagesResponse;
import woozlabs.echo.domain.gmail.dto.message.GmailMessageSendRequest;
import woozlabs.echo.domain.gmail.dto.message.GmailMessageSendResponse;
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
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static woozlabs.echo.global.constant.GlobalConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GmailService {
    // constants
    private final String MULTI_PART_TEXT_PLAIN = "text/plain";
    private final String TEMP_FILE_PREFIX = "echo";
    private final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/gmail.modify",
            "https://mail.google.com/"

    );
    // injection & init
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final MultiThreadGmailService multiThreadGmailService;
    private final MemberRepository memberRepository;
    private final PubSubHistoryRepository pubSubHistoryRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final GmailUtility gmailUtility;
    private final PubSubValidator pubSubValidator;

    public GmailThreadListResponse getQueryUserEmailThreads(String uid, String pageToken, String q) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        ListThreadsResponse response = getQueryListThreadsResponse(pageToken, q, gmailService);
        List<Thread> threads = response.getThreads(); // get threads
        threads = isEmptyResult(threads);
        List<GmailThreadListThreads> detailedThreads = getDetailedThreads(threads, gmailService); // get detailed threads
        Collections.sort(detailedThreads);
        return GmailThreadListResponse.builder()
                .threads(detailedThreads)
                .nextPageToken(response.getNextPageToken())
                .build();
    }

    public GmailDraftListResponse getUserEmailDrafts(String uid, String pageToken, String q) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE)
        );
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        ListDraftsResponse response = getListDraftsResponse(gmailService, pageToken, q);
        List<Draft> drafts = response.getDrafts();
        drafts = isEmptyResult(drafts);
        List<GmailDraftListDrafts> detailedDrafts = getDetailedDrafts(drafts, gmailService);
        return GmailDraftListResponse.builder()
                .drafts(detailedDrafts)
                .nextPageToken(response.getNextPageToken())
                .build();
    }

    public GmailThreadGetResponse getUserEmailThread(String uid, String id) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        Thread thread = getOneThreadResponse(id, gmailService);
        List<GmailThreadGetMessagesResponse> messages = getConvertedMessages(thread.getMessages());
        return GmailThreadGetResponse.builder()
                .id(thread.getId())
                .historyId(thread.getHistoryId())
                .messages(messages)
                .build();
    }

    public GmailThreadTrashResponse trashUserEmailThread(String uid, String id) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        Thread trashedThread = gmailService.users().threads().trash(USER_ID, id)
                .setPrettyPrint(Boolean.TRUE)
                .execute();
        return new GmailThreadTrashResponse(trashedThread.getId());
    }

    public GmailThreadDeleteResponse deleteUserEmailThread(String uid, String id) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        gmailService.users().threads().delete(USER_ID, id)
                .setPrettyPrint(Boolean.TRUE)
                .execute();
        return new GmailThreadDeleteResponse(id);
    }

    public GmailThreadSearchListResponse searchUserEmailThreads(String uid, GmailSearchParams params) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        ListThreadsResponse response = getSearchListThreadsResponse(params, gmailService);
        List<Thread> threads = response.getThreads();
        threads = isEmptyResult(threads);
        List<GmailThreadSearchListThreads> searchedThreads = getSimpleThreads(threads); // get detailed threads
        return GmailThreadSearchListResponse.builder()
                .threads(searchedThreads)
                .nextPageToken(response.getNextPageToken())
                .build();
    }

    public void getUserEmailMessage(String uid, String messageId) throws Exception {
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        Message message = gmailService.users().messages().get(USER_ID, messageId).execute();
        System.out.println(message);
    }

    public GmailMessageAttachmentResponse getAttachment(String uid, String messageId, String id) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        MessagePartBody attachment = gmailService.users().messages()
                .attachments()
                .get(USER_ID, messageId, id)
                .execute();
        return GmailMessageAttachmentResponse.builder()
                .attachmentId(attachment.getAttachmentId())
                .size(attachment.getSize())
                .data(attachment.getData()).build();
    }

    public GmailMessageSendResponse sendUserEmailMessage(String uid, GmailMessageSendRequest request) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
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
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        int totalCount = getTotalCountThreads(gmailService, label);
        return GmailThreadTotalCountResponse.builder()
                .totalCount(totalCount)
                .build();
    }

    public GmailDraftSendResponse sendUserEmailDraft(String uid, GmailDraftCommonRequest request) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
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
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        Draft draft = getOneDraftResponse(id, gmailService);
        GmailDraftGetMessage message = GmailDraftGetMessage.toGmailDraftGetMessages(draft.getMessage());
        return GmailDraftGetResponse.builder()
                .id(draft.getId())
                .message(message)
                .build();
    }

    public GmailDraftUpdateResponse updateUserEmailDraft(String uid, String id, GmailDraftCommonRequest request) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
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
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        Profile profile = gmailService.users().getProfile(USER_ID).execute();
        String fromEmailAddress = profile.getEmailAddress();
        request.setFromEmailAddress(fromEmailAddress);
        MimeMessage mimeMessage = createDraft(request);
        Message message = createMessage(mimeMessage);
        // create new draft
        Draft draft = new Draft().setMessage(message);
        draft = gmailService.users().drafts().create(USER_ID, draft).execute();
        GmailDraftGetMessage changedMessage = GmailDraftGetMessage.toGmailDraftGetMessages(draft.getMessage());
        return GmailDraftCreateResponse.builder()
                .id(draft.getId())
                .message(changedMessage)
                .build();
    }

    public GmailThreadUpdateResponse updateUserEmailThread(String uid, String id, GmailThreadUpdateRequest request) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        ModifyThreadRequest modifyThreadRequest = new ModifyThreadRequest();
        modifyThreadRequest.setAddLabelIds(request.getAddLabelIds());
        modifyThreadRequest.setRemoveLabelIds(request.getRemoveLabelIds());
        Thread thread = gmailService.users().threads().modify(USER_ID, id, modifyThreadRequest).execute();
        return GmailThreadUpdateResponse.builder()
                .addLabelIds(request.getAddLabelIds())
                .removeLabelIds(request.getRemoveLabelIds())
                .build();
    }

    @Transactional
    public PubSubWatchResponse subscribePubSub(String uid, PubSubWatchRequest dto) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        List<FcmToken> fcmTokens = fcmTokenRepository.findByMember(member);
        pubSubValidator.validateWatch(fcmTokens);
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        WatchRequest watchRequest = new WatchRequest()
                .setLabelIds(dto.getLabelIds())
                .setLabelFilterBehavior("include")
                .setTopicName("projects/echo-email-app/topics/gmail");
        WatchResponse watchResponse = gmailService.users().watch(USER_ID, watchRequest).execute();
        Optional<PubSubHistory> pubSubHistory = pubSubHistoryRepository.findByMember(member);
        if(pubSubHistory.isEmpty()){
            PubSubHistory newHistory = PubSubHistory.builder()
                    .historyId(watchResponse.getHistoryId())
                    .member(member).build();
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
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE
                        , ErrorCode.NOT_FOUND_ACCESS_TOKEN.getMessage())
        );
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        gmailService.users().stop(USER_ID).execute();
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
                log.error(REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG);
                throw new GmailException(REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG);
            }
        }).collect(Collectors.toList());
    }

    private Gmail createGmailService(String accessToken) throws Exception{
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        HttpRequestInitializer requestInitializer = createCredentialWithAccessToken(accessToken);
        return new Gmail.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName("Echo")
                .build();
    }

    private List<GmailDraftListDrafts> getDetailedDrafts(List<Draft> drafts, Gmail gmailService) {
        List<CompletableFuture<Optional<GmailDraftListDrafts>>> futures = drafts.stream()
                .map((draft) -> multiThreadGmailService.asyncRequestGmailDraftGetForList(draft, gmailService)
                        .thenApply(Optional::of)
                        .exceptionally(error -> {
                            log.error(error.getMessage());
                            return Optional.empty();
                        })
                ).toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return futures.stream().map((future) -> {
            try{
                Optional<GmailDraftListDrafts> result = future.get();
                if(result.isEmpty())throw new GmailException(GlobalConstant.REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG);
                return result.get();
            }catch (InterruptedException | CancellationException | ExecutionException e){
                throw new GmailException(GlobalConstant.REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG);
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

    private List<GmailThreadGetMessagesResponse> getConvertedMessages(List<Message> messages){
        return messages.stream().map((message) -> GmailThreadGetMessagesResponse.toGmailThreadGetMessages(message, gmailUtility)).toList();
    }

    private ListThreadsResponse getQueryListThreadsResponse(String pageToken, String q, Gmail gmailService) throws IOException {
        return gmailService.users().threads()
                .list(USER_ID)
                .setMaxResults(THREADS_LIST_MAX_LENGTH)
                .setPageToken(pageToken)
                .setPrettyPrint(Boolean.TRUE)
                .setQ(q)
                .execute();
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

    private Thread getOneThreadResponse(String id, Gmail gmailService) throws IOException{
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

    private HttpRequestInitializer createCredentialWithAccessToken(String accessToken){
        AccessToken token = AccessToken.newBuilder()
                .setTokenValue(accessToken)
                .setScopes(SCOPES)
                .build();
        GoogleCredentials googleCredentials = GoogleCredentials.create(token);
        return new HttpCredentialsAdapter(googleCredentials);
    }

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

        for(MultipartFile mimFile : request.getFiles()){
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
}