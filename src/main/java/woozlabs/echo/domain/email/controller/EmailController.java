package woozlabs.echo.domain.email.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.email.service.EmailService;
import woozlabs.echo.global.dto.ResponseDto;

@RestController
@Slf4j
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    @GetMapping("/api/v1/email/messages")
    public ResponseEntity<ResponseDto> getEmails(@RequestParam("accessToken") String accessToken){
        log.info("Request to get my emails");
        try {
            emailService.getEmailMessages(accessToken);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/v1/email/sync/messages")
    public ResponseEntity<ResponseDto> syncGetEmails(@RequestParam("accessToken") String accessToken){
        log.info("Sync request to get my emails");
        try {
            emailService.syncGetEmailMessages(accessToken);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}