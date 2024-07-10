package woozlabs.echo.domain.gmail.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import woozlabs.echo.domain.gmail.dto.*;
import woozlabs.echo.domain.gmail.service.GmailService;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.dto.ResponseDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GmailController {
    private final GmailService gmailService;

    // threads
    @GetMapping("/api/v1/gmail/threads/inbox")
    public ResponseEntity<ResponseDto> getInboxThreads(HttpServletRequest httpServletRequest,
                                                       @RequestParam(value = "pageToken", required = false) String pageToken,
                                                       @RequestParam(value = "category", required = false, defaultValue = "category:primary") String category){
        log.info("Request to get threads");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailThreadListResponse response = gmailService.getUserEmailThreads(uid, pageToken, category);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/v1/gmail/threads")
    public ResponseEntity<ResponseDto> searchThreads(@RequestParam(value = "from", required = false) String from,
                                                     @RequestParam(value = "to", required = false) String to,
                                                     @RequestParam(value = "subject", required = false) String subject,
                                                     @RequestParam(value = "q", required = false) String query, HttpServletRequest httpServletRequest){
        log.info("Request to search threads");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailSearchParams params = GmailSearchParams.builder()
                    .from(from).to(to).subject(subject).query(query).build();
            GmailThreadListSearchResponse response = gmailService.searchUserEmailThreads(uid, params);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/v1/gmail/threads/{id}")
    public ResponseEntity<ResponseDto> getThread(HttpServletRequest httpServletRequest, @PathVariable("id") String id){
        log.info("Request to get thread");
        try{
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailThreadGetResponse response = gmailService.getUserEmailThread(uid, id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/api/v1/gmail/threads/{id}/trash")
    public ResponseEntity<ResponseDto> trashThread(HttpServletRequest httpServletRequest, @PathVariable("id") String id){
        log.info("Request to trash thread");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailThreadTrashResponse response = gmailService.trashUserEmailThread(uid, id);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/api/v1/gmail/threads/{id}")
    public ResponseEntity<ResponseDto> deleteThread(HttpServletRequest httpServletRequest, @PathVariable("id") String id){
        log.info("Request to delete thread");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailThreadDeleteResponse response = gmailService.deleteUserEmailThread(uid, id);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // messages
    @GetMapping("/api/v1/gmail/messages/{messageId}/attachments/{id}")
    public ResponseEntity<ResponseDto> getAttachment(HttpServletRequest httpServletRequest,
                                                     @PathVariable("messageId") String messageId, @PathVariable("id") String id){
        log.info("Request to get attachment in message");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailMessageAttachmentResponse response = gmailService.getAttachment(uid, messageId, id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(value = "/api/v1/gmail/messages/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> sendMessage(HttpServletRequest httpServletRequest,
                                                   @RequestPart("toEmailAddress") String toEmailAddress,
                                                   @RequestPart("subject") String subject,
                                                   @RequestPart("bodyText") String bodyText,
                                                   @RequestPart(value = "files", required = false) List<MultipartFile> files){
        log.info("Request to send message");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GmailMessageSendRequest request = new GmailMessageSendRequest();
            request.setToEmailAddress(toEmailAddress);
            request.setSubject(subject);
            request.setBodyText(bodyText);
            request.setFiles(files);
            GmailMessageSendResponse response = gmailService.sendUserEmailMessage(uid, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}