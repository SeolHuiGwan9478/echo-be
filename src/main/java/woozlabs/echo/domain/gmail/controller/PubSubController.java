package woozlabs.echo.domain.gmail.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.gmail.dto.pubsub.FcmTokenRequest;
import woozlabs.echo.domain.gmail.dto.pubsub.FcmTokenResponse;
import woozlabs.echo.domain.gmail.dto.pubsub.GetVerificationDataResponse;
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
    public ResponseEntity<ResponseDto> handleGmailWebhook(@RequestBody PubSubMessage pubsubMessage){
        log.info("Request to webhook from gcp pub/sub");
        try{
            pubSubService.handleFirebaseCloudMessage(pubsubMessage);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            log.error(e.getMessage()); // checking bug
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/v1/fcm")
    public ResponseEntity<ResponseDto> postFcmToken(HttpServletRequest httpServletRequest, @RequestBody FcmTokenRequest request){
        log.info("Request to post fcmToken");
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        FcmTokenResponse response = pubSubService.saveFcmToken(uid, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/verification/{uuid}")
    public ResponseEntity<?> getVerificationData(HttpServletRequest httpServletRequest, @PathVariable String uuid){
        log.info("Request to get verification data");
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        GetVerificationDataResponse response = pubSubService.getVerificationData(uid, uuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}