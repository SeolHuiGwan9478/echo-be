package woozlabs.echo.domain.gmail.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.gmail.dto.*;
import woozlabs.echo.domain.gmail.service.GmailService;
import woozlabs.echo.global.dto.ResponseDto;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GmailController {
    private final GmailService gmailService;

    @GetMapping("/api/v1/gmail/threads/inbox")
    public ResponseEntity<ResponseDto> getInboxThreads(@RequestParam("accessToken") String accessToken,
                                                  @RequestParam(value = "pageToken", required = false) String pageToken,
                                                  @RequestParam(value = "category", required = false, defaultValue = "category:primary") String category){
        log.info("Request to get threads");
        try {
            GmailThreadListResponse response = gmailService.getUserEmailThreads(accessToken, pageToken, category);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/v1/gmail/threads")
    public ResponseEntity<ResponseDto> searchThreads(@RequestParam(value = "from", required = false) String from,
                                                     @RequestParam(value = "to", required = false) String to,
                                                     @RequestParam(value = "subject", required = false) String subject,
                                                     @RequestParam(value = "q", required = false) String query, @RequestParam("accessToken") String accessToken){
        log.info("Request to search threads");
        try {
            GmailSearchParams params = GmailSearchParams.builder()
                    .from(from).to(to).subject(subject).query(query).build();
            GmailThreadListSearchResponse response = gmailService.searchUserEmailThreads(accessToken, params);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/v1/gmail/threads/{id}")
    public ResponseEntity<ResponseDto> getThread(@RequestParam("accessToken") String accessToken, @PathVariable("id") String id){
        log.info("Request to get thread");
        try{
            GmailThreadGetResponse response = gmailService.getUserEmailThread(accessToken, id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/api/v1/gmail/threads/{id}/trash")
    public ResponseEntity<ResponseDto> trashThread(@RequestParam("accessToken") String accessToken, @PathVariable("id") String id){
        log.info("Request to trash thread");
        try {
            GmailThreadTrashResponse response = gmailService.trashUserEmailThread(accessToken, id);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/api/v1/gmail/threads/{id}")
    public ResponseEntity<ResponseDto> deleteThread(@RequestParam("accessToken") String accessToken, @PathVariable("id") String id){
        log.info("Request to delete thread");
        try {
            GmailThreadDeleteResponse response = gmailService.deleteUserEmailThread(accessToken, id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/v1/gmail/messages/send")
    public ResponseEntity<ResponseDto> sendMessage(@RequestBody GmailMessageSendRequest request){
        log.info("Request to send message");
        try {
            return new ResponseEntity<>(HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
