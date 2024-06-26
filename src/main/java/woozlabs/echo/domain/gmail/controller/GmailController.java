package woozlabs.echo.domain.gmail.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.gmail.dto.GmailThreadListResponse;
import woozlabs.echo.domain.gmail.service.GmailService;
import woozlabs.echo.global.dto.ResponseDto;

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
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
