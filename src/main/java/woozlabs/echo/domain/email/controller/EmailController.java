package woozlabs.echo.domain.email.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.email.dto.GetUserEmailMessagesResponse;
import woozlabs.echo.domain.email.service.EmailService;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.dto.ErrorResponse;
import woozlabs.echo.global.dto.ResponseDto;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    @GetMapping("/api/v1/email/messages")
    public ResponseEntity<ResponseDto> getEmails(@RequestParam("accessToken") String accessToken){
        log.info("Request to get my emails");
        try {
            GetUserEmailMessagesResponse response = emailService.getEmailMessages(accessToken);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (JsonProcessingException e){
            Map<String, String> errors = new HashMap<>();
            errors.put(GlobalConstant.EMAIL_ERR_MSG_KEY, e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(errors);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}