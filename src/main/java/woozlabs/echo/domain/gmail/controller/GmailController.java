package woozlabs.echo.domain.gmail.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import woozlabs.echo.domain.gmail.dto.autoForwarding.AutoForwardingResponse;
import woozlabs.echo.domain.gmail.dto.draft.*;
import woozlabs.echo.domain.gmail.dto.history.GmailHistoryListResponse;
import woozlabs.echo.domain.gmail.dto.message.*;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadTotalCountResponse;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubWatchRequest;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubWatchResponse;
import woozlabs.echo.domain.gmail.dto.thread.*;
import woozlabs.echo.domain.gmail.service.GmailService;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.dto.ResponseDto;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.springframework.web.servlet.function.RequestPredicates.contentType;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GmailController {
    private final GmailService gmailService;
    // threads
    @GetMapping("/api/v1/gmail/threads")
    public ResponseEntity<ResponseDto> getQueryThreads(HttpServletRequest httpServletRequest,
                                                       @RequestParam(value = "pageToken", required = false) String pageToken,
                                                       @RequestParam(value = "maxResults", required = false, defaultValue = "50") Long maxResults,
                                                       @RequestParam(value = "q") String q){
        log.info("Request to get threads");
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        GmailThreadListResponse response = gmailService.getQueryUserEmailThreads(uid, pageToken, maxResults, q);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/v1/gmail/threads/search")
    public ResponseEntity<ResponseDto> searchThreads(@RequestParam(value = "from", required = false) String from,
                                                     @RequestParam(value = "to", required = false) String to,
                                                     @RequestParam(value = "subject", required = false) String subject,
                                                     @RequestParam(value = "q", required = false) String query, HttpServletRequest httpServletRequest){
        log.info("Request to search threads");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailSearchParams params = GmailSearchParams.builder()
                    .from(from).to(to).subject(subject).query(query).build();
            GmailThreadSearchListResponse response = gmailService.searchUserEmailThreads(uid, params);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, e.getMessage());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/v1/gmail/threads/{id}")
    public ResponseEntity<ResponseDto> getThread(HttpServletRequest httpServletRequest, @PathVariable("id") String id){
        log.info("Request to get thread");
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        GmailThreadGetResponse response = gmailService.getUserEmailThread(uid, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/api/v1/gmail/threads/{id}/trash")
    public ResponseEntity<ResponseDto> trashThread(HttpServletRequest httpServletRequest, @PathVariable("id") String id){
        log.info("Request to trash thread");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailThreadTrashResponse response = gmailService.trashUserEmailThread(uid, id);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, e.getMessage());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/api/v1/gmail/threads/{id}")
    public ResponseEntity<ResponseDto> deleteThread(HttpServletRequest httpServletRequest, @PathVariable("id") String id){
        log.info("Request to delete thread");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailThreadDeleteResponse response = gmailService.deleteUserEmailThread(uid, id);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, e.getMessage());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/api/v1/gmail/threads/{id}/modify")
    public ResponseEntity<ResponseDto> updateThread(HttpServletRequest httpServletRequest,
                                                    @PathVariable("id") String id,
                                                    @RequestBody GmailThreadUpdateRequest request){
        log.info("Request to update thread");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailThreadUpdateResponse response = gmailService.updateUserEmailThread(uid, id, request);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, e.getMessage());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/v1/gmail/threads/count")
    public ResponseEntity<ResponseDto> getThreadsTotalCount(HttpServletRequest httpServletRequest,
                                                            @RequestParam("label") String label){
        log.info("Request to get total count of messages");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailThreadTotalCountResponse response = gmailService.getUserEmailThreadsTotalCount(uid, label);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, e.getMessage());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    // messages
    @GetMapping("/api/v1/gmail/messages/{messageId}")
    public ResponseEntity<?> getMessage(HttpServletRequest httpServletRequest,
                                                  @PathVariable("messageId") String messageId){
        log.info("Request to get message({})", messageId);
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailMessageGetResponse response = gmailService.getUserEmailMessage(uid, messageId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/api/v1/gmail/messages/{messageId}/modify")
    public ResponseEntity<?> updateMessage(HttpServletRequest httpServletRequest,
                                           @PathVariable("messageId") String messageId,
                                           @RequestBody GmailMessageUpdateRequest request){
        log.info("Request to update message");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailMessageUpdateResponse response = gmailService.updateUserEmailMessage(uid, messageId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/v1/gmail/messages/{messageId}/attachments/{id}")
    public ResponseEntity<?> getAttachment(HttpServletRequest httpServletRequest,
                                                     @PathVariable("messageId") String messageId, @PathVariable("id") String id){
        log.info("Request to get attachment in message");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailMessageAttachmentResponse response = gmailService.getAttachment(uid, messageId, id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, e.getMessage());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/v1/gmail/messages/{messageId}/attachments/{id}/download")
    public ResponseEntity<?> downloadAttachment(HttpServletRequest httpServletRequest,
                                                @PathVariable("messageId") String messageId, @PathVariable("id") String attachmentId){
        log.info("Request to download attachment in message");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailMessageAttachmentDownloadResponse response = gmailService.downloadAttachment(uid, messageId, attachmentId);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "echo-test.pdf");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(response.getByteData().length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(response.getByteData());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/api/v1/gmail/messages/send")
    public ResponseEntity<?> sendMessage(HttpServletRequest httpServletRequest,
                                                   @RequestParam("toEmailAddress") String toEmailAddress,
                                                   @RequestParam("subject") String subject,
                                                   @RequestParam("bodyText") String bodyText,
                                                   @RequestParam(value = "files", required = false) List<MultipartFile> files){
        log.info("Request to send message");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailMessageSendRequest request = new GmailMessageSendRequest();
            request.setToEmailAddress(toEmailAddress);
            request.setSubject(subject);
            request.setBodyText(bodyText);
            request.setFiles(Objects.requireNonNullElseGet(files, ArrayList::new));
            GmailMessageSendResponse response = gmailService.sendUserEmailMessage(uid, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, e.getMessage());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/v1/gmail/drafts/{id}")
    public ResponseEntity<ResponseDto> getDraft(HttpServletRequest httpServletRequest, @PathVariable("id") String id){
        log.info("Request to get draft");
        try{
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailDraftGetResponse response = gmailService.getUserEmailDraft(uid, id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, e.getMessage());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/api/v1/gmail/drafts/create")
    public ResponseEntity<?> createDraft(HttpServletRequest httpServletRequest,
                                                   @RequestParam("toEmailAddress") String toEmailAddress,
                                                   @RequestParam("subject") String subject,
                                                   @RequestParam("bodyText") String bodyText,
                                                   @RequestParam(value = "files", required = false) List<MultipartFile> files){
        log.info("Request to create draft");
        try{
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailDraftCommonRequest request = new GmailDraftCommonRequest();
            request.setToEmailAddress(toEmailAddress);
            request.setSubject(subject);
            request.setBodyText(bodyText);
            request.setFiles(files);
            GmailDraftCreateResponse response = gmailService.createUserEmailDraft(uid, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, e.getMessage());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/api/v1/gmail/drafts/{id}/modify")
    public ResponseEntity<?> modifyDraft(HttpServletRequest httpServletRequest,
                                                   @PathVariable("id") String id,
                                                   @RequestParam("toEmailAddress") String toEmailAddress,
                                                   @RequestParam("subject") String subject,
                                                   @RequestParam("bodyText") String bodyText,
                                                   @RequestParam(value = "files", required = false) List<MultipartFile> files){
        log.info("Request to modify draft");
        try{
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailDraftCommonRequest request = new GmailDraftCommonRequest();
            request.setToEmailAddress(toEmailAddress);
            request.setSubject(subject);
            request.setBodyText(bodyText);
            request.setFiles(files);
            GmailDraftUpdateResponse response = gmailService.updateUserEmailDraft(uid, id, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, e.getMessage());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @PostMapping(value = "/api/v1/gmail/drafts/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendDraft(HttpServletRequest httpServletRequest,
                                                   @RequestParam("toEmailAddress") String toEmailAddress,
                                                   @RequestParam("subject") String subject,
                                                   @RequestParam("bodyText") String bodyText,
                                                   @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        log.info("Request to send draft");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailDraftCommonRequest request = new GmailDraftCommonRequest();
            request.setToEmailAddress(toEmailAddress);
            request.setSubject(subject);
            request.setBodyText(bodyText);
            request.setFiles(files);
            GmailDraftSendResponse response = gmailService.sendUserEmailDraft(uid, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IOException e) {
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }
    @PostMapping("/api/v1/gmail/watch")
    public ResponseEntity<?> postWatch(HttpServletRequest httpServletRequest, @RequestBody PubSubWatchRequest request){
        log.info("Request to watch pub/sub");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            PubSubWatchResponse response = gmailService.subscribePubSub(uid, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.CLOUD_PUB_SUB_WATCH_ERR, e.getMessage());
        }catch (CustomErrorException e){
            throw e;
        } catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/api/v1/gmail/stop")
    public ResponseEntity<?> getStop(HttpServletRequest httpServletRequest){
        log.info("Request to stop pub/sub");
        try{
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            gmailService.stopPubSub(uid);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (IOException e){
            throw new CustomErrorException(ErrorCode.CLOUD_PUB_SUB_STOP_ERR, e.getMessage());
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/v1/gmail/histories")
    public ResponseEntity<?> getHistories(HttpServletRequest httpServletRequest,
                                                    @RequestParam("historyId") String historyId,
                                                    @RequestParam(value = "pageToken", required = false) String pageToken){
        log.info("Request to get histories from {}", historyId);
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailHistoryListResponse response = gmailService.getHistories(uid, historyId, pageToken);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/api/v1/gmail/auto-forwarding")
    public ResponseEntity<?> setAutoForwarding(HttpServletRequest httpServletRequest, @RequestParam("q") String q, @RequestParam("email") String email){
        log.info("Request to set auto forwarding");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            AutoForwardingResponse response = gmailService.setUpAutoForwarding(uid, q, email);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/api/v1/gmail/gen-verification-label")
    public ResponseEntity<?> generateVerificationLabel(HttpServletRequest httpServletRequest) {
        log.info("Request to generate verification label");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            gmailService.generateVerificationLabel(uid);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }
}