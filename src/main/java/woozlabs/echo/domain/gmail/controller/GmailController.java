package woozlabs.echo.domain.gmail.controller;

import com.google.api.Http;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.gmail.dto.GmailThreadDeleteResponse;
import woozlabs.echo.domain.gmail.dto.GmailThreadGetResponse;
import woozlabs.echo.domain.gmail.dto.GmailThreadListResponse;
import woozlabs.echo.domain.gmail.service.GmailService;
import woozlabs.echo.global.dto.ResponseDto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GmailController {
    private final GmailService gmailService;

    @GetMapping("/api/v1/gmail/threads")
    public ResponseEntity<ResponseDto> getThreads(@RequestParam("accessToken") String accessToken,
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

    @DeleteMapping("/api/v1/gmail/threads/{id}")
    public ResponseEntity<ResponseDto> deleteThread(@RequestParam("accessToken") String accessToken, @PathVariable("id") String id){
        log.info("Request to delete thread");
        try {
            GmailThreadDeleteResponse response = gmailService.deleteUserEmailThread(accessToken, id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
