package woozlabs.echo.domain.gmail.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.gmail.dto.pubsub.PubSubMessage;
import woozlabs.echo.domain.gmail.service.GmailService;
import woozlabs.echo.domain.gmail.service.PubSubService;
import woozlabs.echo.global.dto.ResponseDto;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PubSubController {
    private final PubSubService pubSubService;

    @PostMapping(value = "/api/v1/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto> handleGmailWebhook(@RequestBody PubSubMessage pubsubMessage) throws JsonProcessingException {
        log.info("Request to webhook from gcp pub/sub");
        pubSubService.handleFirebaseCloudMessage(pubsubMessage);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
