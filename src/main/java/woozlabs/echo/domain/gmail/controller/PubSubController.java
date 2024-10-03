package woozlabs.echo.domain.gmail.controller;

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
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.dto.ResponseDto;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PubSubController {
    private final PubSubService pubSubService;
    private final GmailUtility gmailUtility;

    @PostMapping(value = "/api/v1/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto> handleGmailWebhook(@RequestBody PubSubMessage pubsubMessage){
        log.info("Request to webhook from gcp pub/sub");
        try{
            pubSubService.handleFirebaseCloudMessage(pubsubMessage);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/v1/fcm")
    public ResponseEntity<ResponseDto> postFcmToken(HttpServletRequest httpServletRequest, @RequestBody FcmTokenRequest request, @RequestParam("aAUid") String aAUid){
        log.info("Request to post fcmToken");
        String uid = gmailUtility.getActiveAccountUid(httpServletRequest, aAUid);
        FcmTokenResponse response = pubSubService.saveFcmToken(uid, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/verification/{uuid}")
    public ResponseEntity<?> getVerificationData(@PathVariable("uuid") String uuid){
        log.info("Request to get verification data");
        GetVerificationDataResponse response = pubSubService.getVerificationData(uuid);
        String link = response.getLinks().split(",")[0];
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", link)
                .build();
    }
}