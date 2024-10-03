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
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.global.dto.ResponseDto;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GmailController {
    private final GmailService gmailService;
    private final GmailUtility gmailUtility;
    // threads
    @GetMapping("/api/v1/gmail/threads")
    public ResponseEntity<ResponseDto> getQueryThreads(HttpServletRequest httpServletRequest,
                                                       @RequestParam(value = "pageToken", required = false) String pageToken,
                                                       @RequestParam(value = "maxResults", required = false, defaultValue = "50") Long maxResults,
                                                       @RequestParam(value = "q") String q,
                                                       @RequestParam("aAUid") String aAUid){
        log.info("Request to get threads");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailThreadListResponse response = gmailService.getQueryUserEmailThreads(accessToken, pageToken, maxResults, q);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/v1/gmail/threads/search")
    public ResponseEntity<ResponseDto> searchThreads(@RequestParam(value = "from", required = false) String from,
                                                     @RequestParam(value = "to", required = false) String to,
                                                     @RequestParam(value = "subject", required = false) String subject,
                                                     @RequestParam(value = "q", required = false) String query, HttpServletRequest httpServletRequest,
                                                     @RequestParam("aAUid") String aAUid){
        log.info("Request to search threads");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailSearchParams params = GmailSearchParams.builder()
                .from(from).to(to).subject(subject).query(query).build();
        GmailThreadSearchListResponse response = gmailService.searchUserEmailThreads(accessToken, params);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/v1/gmail/threads/{id}")
    public ResponseEntity<ResponseDto> getThread(HttpServletRequest httpServletRequest, @PathVariable("id") String id,
                                                 @RequestParam("aAUid") String aAUid){
        log.info("Request to get thread");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailThreadGetResponse response = gmailService.getUserEmailThread(accessToken, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/api/v1/gmail/threads/{id}/trash")
    public ResponseEntity<ResponseDto> trashThread(HttpServletRequest httpServletRequest, @PathVariable("id") String id,
                                                   @RequestParam("aAUid") String aAUid){
        log.info("Request to trash thread");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailThreadTrashResponse response = gmailService.trashUserEmailThread(accessToken, id);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/api/v1/gmail/threads/{id}")
    public ResponseEntity<ResponseDto> deleteThread(HttpServletRequest httpServletRequest, @PathVariable("id") String id,
                                                    @RequestParam("aAUid") String aAUid){
        log.info("Request to delete thread");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailThreadDeleteResponse response = gmailService.deleteUserEmailThread(accessToken, id);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/api/v1/gmail/threads/{id}/modify")
    public ResponseEntity<ResponseDto> updateThread(HttpServletRequest httpServletRequest,
                                                    @PathVariable("id") String id,
                                                    @RequestBody GmailThreadUpdateRequest request,
                                                    @RequestParam("aAUid") String aAUid){
        log.info("Request to update thread");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailThreadUpdateResponse response = gmailService.updateUserEmailThread(accessToken, id, request);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @GetMapping("/api/v1/gmail/threads/count")
    public ResponseEntity<ResponseDto> getThreadsTotalCount(HttpServletRequest httpServletRequest,
                                                            @RequestParam("label") String label,
                                                            @RequestParam("aAUid") String aAUid){
        log.info("Request to get total count of messages");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailThreadTotalCountResponse response = gmailService.getUserEmailThreadsTotalCount(accessToken, label);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // messages
    @GetMapping("/api/v1/gmail/messages/{messageId}")
    public ResponseEntity<?> getMessage(HttpServletRequest httpServletRequest,
                                                  @PathVariable("messageId") String messageId,
                                        @RequestParam("aAUid") String aAUid){
        log.info("Request to get message({})", messageId);
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailMessageGetResponse response = gmailService.getUserEmailMessage(accessToken, messageId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/api/v1/gmail/messages/{messageId}/modify")
    public ResponseEntity<?> updateMessage(HttpServletRequest httpServletRequest,
                                           @PathVariable("messageId") String messageId,
                                           @RequestBody GmailMessageUpdateRequest request,
                                           @RequestParam("aAUid") String aAUid){
        log.info("Request to update message");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailMessageUpdateResponse response = gmailService.updateUserEmailMessage(accessToken, messageId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/v1/gmail/messages/{messageId}/attachments/{id}")
    public ResponseEntity<?> getAttachment(HttpServletRequest httpServletRequest,
                                                     @PathVariable("messageId") String messageId, @PathVariable("id") String id,
                                           @RequestParam("aAUid") String aAUid){
        log.info("Request to get attachment in message");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailMessageAttachmentResponse response = gmailService.getAttachment(accessToken, messageId, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/v1/gmail/messages/{messageId}/attachments/{id}/download")
    public ResponseEntity<?> downloadAttachment(HttpServletRequest httpServletRequest,
                                                @PathVariable("messageId") String messageId, @PathVariable("id") String attachmentId,
                                                @RequestParam("fileName") String fileName,
                                                @RequestParam("aAUid") String aAUid){
        log.info("Request to download attachment in message");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailMessageAttachmentDownloadResponse response = gmailService.downloadAttachment(accessToken, messageId, attachmentId);
        HttpHeaders headers = new HttpHeaders(); // set response header
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(response.getByteData().length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(response.getByteData());
    }

    @GetMapping("/api/v1/gmail/messages/{messageId}/google-drive-attachments")
    public ResponseEntity<?> downloadGoogleDriveAttachment(HttpServletRequest httpServletRequest,
                                                            @PathVariable("messageId") String messageId,
                                                           @RequestParam("aAUid") String aAUid){
        log.info("Request to download google drive attachment in message");
        try {
            String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
            gmailService.getGoogleDriveFileId(accessToken, messageId);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/api/v1/gmail/messages/send")
    public ResponseEntity<?> sendMessage(HttpServletRequest httpServletRequest,
                                                   @RequestParam("toEmailAddress") String toEmailAddress,
                                                   @RequestParam("subject") String subject,
                                                   @RequestParam("bodyText") String bodyText,
                                                   @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                         @RequestParam("aAUid") String aAUid){
        log.info("Request to send message");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailMessageSendRequest request = new GmailMessageSendRequest();
        request.setToEmailAddress(toEmailAddress);
        request.setSubject(subject);
        request.setBodyText(bodyText);
        request.setFiles(Objects.requireNonNullElseGet(files, ArrayList::new));
        GmailMessageSendResponse response = gmailService.sendUserEmailMessage(accessToken, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/gmail/drafts/{id}")
    public ResponseEntity<ResponseDto> getDraft(HttpServletRequest httpServletRequest, @PathVariable("id") String id,
                                                @RequestParam("aAUid") String aAUid){
        log.info("Request to get draft");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailDraftGetResponse response = gmailService.getUserEmailDraft(accessToken, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/v1/gmail/drafts/create")
    public ResponseEntity<?> createDraft(HttpServletRequest httpServletRequest,
                                                   @RequestParam("toEmailAddress") String toEmailAddress,
                                                   @RequestParam("subject") String subject,
                                                   @RequestParam("bodyText") String bodyText,
                                                   @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                         @RequestParam("aAUid") String aAUid){
        log.info("Request to create draft");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailDraftCommonRequest request = new GmailDraftCommonRequest();
        request.setToEmailAddress(toEmailAddress);
        request.setSubject(subject);
        request.setBodyText(bodyText);
        request.setFiles(files);
        GmailDraftCreateResponse response = gmailService.createUserEmailDraft(accessToken, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/api/v1/gmail/drafts/{id}/modify")
    public ResponseEntity<?> modifyDraft(HttpServletRequest httpServletRequest,
                                                   @PathVariable("id") String id,
                                                   @RequestParam("toEmailAddress") String toEmailAddress,
                                                   @RequestParam("subject") String subject,
                                                   @RequestParam("bodyText") String bodyText,
                                                   @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                         @RequestParam("aAUid") String aAUid){
        log.info("Request to modify draft");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailDraftCommonRequest request = new GmailDraftCommonRequest();
        request.setToEmailAddress(toEmailAddress);
        request.setSubject(subject);
        request.setBodyText(bodyText);
        request.setFiles(files);
        GmailDraftUpdateResponse response = gmailService.updateUserEmailDraft(accessToken, id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/api/v1/gmail/drafts/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendDraft(HttpServletRequest httpServletRequest,
                                                   @RequestParam("toEmailAddress") String toEmailAddress,
                                                   @RequestParam("subject") String subject,
                                                   @RequestParam("bodyText") String bodyText,
                                                   @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                       @RequestParam("aAUid") String aAUid) {
        log.info("Request to send draft");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailDraftCommonRequest request = new GmailDraftCommonRequest();
        request.setToEmailAddress(toEmailAddress);
        request.setSubject(subject);
        request.setBodyText(bodyText);
        request.setFiles(files);
        GmailDraftSendResponse response = gmailService.sendUserEmailDraft(accessToken, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/api/v1/gmail/watch")
    public ResponseEntity<?> postWatch(HttpServletRequest httpServletRequest, @RequestBody PubSubWatchRequest request,
                                       @RequestParam("aAUid") String aAUid){
        log.info("Request to watch pub/sub");
        Account activeAccount = gmailUtility.getActiveAccount(httpServletRequest, aAUid);
        PubSubWatchResponse response = gmailService.subscribePubSub(activeAccount, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/v1/gmail/stop")
    public ResponseEntity<?> getStop(HttpServletRequest httpServletRequest,
                                     @RequestParam("aAUid") String aAUid){
        log.info("Request to stop pub/sub");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        gmailService.stopPubSub(accessToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/v1/gmail/histories")
    public ResponseEntity<?> getHistories(HttpServletRequest httpServletRequest,
                                                    @RequestParam("historyId") String historyId,
                                                    @RequestParam(value = "pageToken", required = false) String pageToken,
                                          @RequestParam("aAUid") String aAUid){
        log.info("Request to get histories from {}", historyId);
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        GmailHistoryListResponse response = gmailService.getHistories(accessToken, historyId, pageToken);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/v1/gmail/auto-forwarding")
    public ResponseEntity<?> setAutoForwarding(HttpServletRequest httpServletRequest, @RequestParam("q") String q, @RequestParam("email") String email,
                                               @RequestParam("aAUid") String aAUid){
        log.info("Request to set auto forwarding");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        AutoForwardingResponse response = gmailService.setUpAutoForwarding(accessToken, q, email);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/v1/gmail/gen-verification-label")
    public ResponseEntity<?> generateVerificationLabel(HttpServletRequest httpServletRequest, @RequestParam("aAUid") String aAUid){
        log.info("Request to generate verification label");
        String accessToken = gmailUtility.getActiveAccountAccessToken(httpServletRequest, aAUid);
        gmailService.generateVerificationLabel(accessToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}