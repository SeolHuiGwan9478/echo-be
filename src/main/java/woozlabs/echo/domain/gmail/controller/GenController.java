package woozlabs.echo.domain.gmail.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.gmail.dto.template.ExtractScheduleInfo;
import woozlabs.echo.domain.gmail.dto.template.GenScheduleEmailTemplateRequest;
import woozlabs.echo.domain.gmail.service.GenService;
import woozlabs.echo.domain.gmail.util.GmailUtility;


@Slf4j
@RestController
@RequiredArgsConstructor
public class GenController {
    private final GmailUtility gmailUtility;
    private final GenService genService;

    @PostMapping("/ner-test")
    public ResponseEntity<?> testNer(@RequestBody String text){
        try{
            ExtractScheduleInfo response = gmailUtility.extractSchedule(text);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/v1/gen/template")
    public ResponseEntity<?> genEmailTemplate(HttpServletRequest httpServletRequest, @RequestBody GenScheduleEmailTemplateRequest dto){
        log.info("Request to generate email template");
        String aAUid = gmailUtility.getActiveAccountUid(httpServletRequest);
        genService.generateScheduleEmailTemplate(aAUid, dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}