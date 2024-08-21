package woozlabs.echo.domain.gmail.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.gmail.dto.extract.ExtractScheduleInfo;
import woozlabs.echo.domain.gmail.dto.extract.GenScheduleEmailTemplateResponse;
import woozlabs.echo.domain.gmail.dto.extract.GenScheduleEmailTemplateRequest;
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.dto.ResponseDto;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.io.IOException;
import java.security.GeneralSecurityException;


@Slf4j
@RestController
@RequiredArgsConstructor
public class GenController {
    private final GmailUtility gmailUtility;
    @PostMapping("/ner-test")
    public ResponseEntity<ResponseDto> testNer(@RequestBody String text){
        try{
            ExtractScheduleInfo response = gmailUtility.extractSchedule(text);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/v1/gen/email-template")
    public ResponseEntity<ResponseDto> genEmailTemplate(HttpServletRequest httpServletRequest, @RequestBody GenScheduleEmailTemplateRequest dto){
        try{
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            GenScheduleEmailTemplateResponse response = gmailUtility.generateScheduleEmailTemplate(uid, dto.getContent());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (JsonProcessingException e){
            throw new CustomErrorException(ErrorCode.OBJECT_MAPPER_JSON_PARSING_ERROR_MESSAGE, ErrorCode.NOT_FOUND_ACCESS_TOKEN.getMessage());
        }catch (GeneralSecurityException | IOException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
