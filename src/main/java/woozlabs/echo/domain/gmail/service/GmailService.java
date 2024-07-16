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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import woozlabs.echo.domain.gmail.dto.*;
import woozlabs.echo.domain.gmail.dto.draft.*;
import woozlabs.echo.domain.gmail.dto.message.GmailMessageAttachmentResponse;
import woozlabs.echo.domain.gmail.dto.message.GmailMessageSendRequest;
import woozlabs.echo.domain.gmail.dto.message.GmailMessageSendResponse;
import woozlabs.echo.domain.gmail.dto.thread.*;
import woozlabs.echo.domain.gmail.exception.GmailException;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static woozlabs.echo.global.constant.GlobalConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GmailService {
    private final MemberRepository memberRepository;
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
    private final AsyncGmailService asyncGmailService;

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
        List<GmailThreadGetMessages> messages = getConvertedMessages(thread.getMessages());
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

    public GmailThreadListSearchResponse searchUserEmailThreads(String uid, GmailSearchParams params) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        ListThreadsResponse response = getSearchListThreadsResponse(params, gmailService);
        List<Thread> threads = response.getThreads();
        threads = isEmptyResult(threads);
        List<GmailThreadListThreads> detailedThreads = getDetailedThreads(threads, gmailService); // get detailed threads
        Collections.sort(detailedThreads);
        return GmailThreadListSearchResponse.builder()
                .threads(detailedThreads)
                .nextPageToken(response.getNextPageToken())
                .build();
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

    public GmailDraftSendResponse sendUserEmailDraft(String uid, GmailDraftSendRequest request) throws Exception{
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

    public GmailThreadUpdateResponse updateUserEmailThread(String uid, String id, GmailThreadUpdateRequest request) throws Exception{
        Member member = memberRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();
        Gmail gmailService = createGmailService(accessToken);
        ModifyThreadRequest modifyThreadRequest = new ModifyThreadRequest();
        modifyThreadRequest.setAddLabelIds(request.getAddLabelIds());
        modifyThreadRequest.setRemoveLabelIds(request.getRemoveLabelIds());
        Thread thread = gmailService.users().threads().modify(USER_ID, id, modifyThreadRequest).execute();
        System.out.println("hihihi");
        System.out.println(thread);
        return GmailThreadUpdateResponse.builder()
                .addLabelIds(request.getAddLabelIds())
                .removeLabelIds(request.getRemoveLabelIds())
                .build();
    }

    // Methods : get something
    private List<GmailThreadListThreads> getDetailedThreads(List<Thread> threads, Gmail gmailService) {
        List<CompletableFuture<Optional<GmailThreadListThreads>>> futures = threads.stream()
                .map((thread) -> asyncGmailService.asyncRequestGmailThreadGetForList(thread, gmailService)
                        .thenApply(Optional::of)
                        .exceptionally(error -> {
                            log.error(error.getMessage());
                            return Optional.empty();
                        })
                ).toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // only get first top of message
        return futures.stream().map((future) -> {
            try{
                Optional<GmailThreadListThreads> result = future.get();
                if(result.isEmpty())throw new GmailException(GlobalConstant.REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG);
                return result.get();
            }catch (InterruptedException | CancellationException | ExecutionException e){
                throw new GmailException(GlobalConstant.REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG);
            }
        }).collect(Collectors.toList());
    }

    private List<GmailDraftListDrafts> getDetailedDrafts(List<Draft> drafts, Gmail gmailService) {
        List<CompletableFuture<Optional<GmailDraftListDrafts>>> futures = drafts.stream()
                .map((draft) -> asyncGmailService.asyncRequestGmailDraftGetForList(draft, gmailService)
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

    private List<GmailThreadGetMessages> getConvertedMessages(List<Message> messages){
        return messages.stream().map(GmailThreadGetMessages::toGmailThreadGetMessages).toList();
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

    private Gmail createGmailService(String accessToken) throws Exception{
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        HttpRequestInitializer requestInitializer = createCredentialWithAccessToken(accessToken);
        return new Gmail.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName("Echo")
                .build();
    }

    private MimeMessage createEmail(GmailMessageSendRequest request) throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        // setting base
        email.setFrom(new InternetAddress(request.getFromEmailAddress()));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
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

    private MimeMessage createDraft(GmailDraftSendRequest request) throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        // setting base
        email.setFrom(new InternetAddress(request.getFromEmailAddress()));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
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
}