package woozlabs.echo.domain.gmail.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.gmail.dto.pubsub.FcmTokenResponse;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubMessage;
import woozlabs.echo.domain.gmail.service.PubSubService;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.dto.ResponseDto;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PubSubController {
    private final PubSubService pubSubService;

    @PostMapping(value = "/api/v1/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto> handleGmailWebhook(@RequestBody PubSubMessage pubsubMessage) throws JsonProcessingException {
        log.info("Request to webhook from gcp pub/sub");
        try{
            pubSubService.handleFirebaseCloudMessage(pubsubMessage);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (CustomErrorException e){
            log.error(e.getMessage());
        } catch (Exception e){
            log.error(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/api/v1/fcm")
    public ResponseEntity<ResponseDto> postFcmToken(HttpServletRequest httpServletRequest, @RequestParam("fcmToken") String fcmToken){
        log.info("Request to post fcmToken");
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        FcmTokenResponse response = pubSubService.saveFcmToken(uid, fcmToken);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}