package woozlabs.echo.domain.email.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import woozlabs.echo.domain.email.dto.*;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.constant.GlobalConstant;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class EmailService {
    // dependency injection
    private final RestTemplate restTemplate;
    private final ObjectMapper om;
    private final MemberRepository memberRepository;
    private final AsyncEmailService asyncEmailService;
    // constant
    private final int MESSAGES_MAX_RESULTS = 10;
    public void getEmailMessages(String accessToken) throws JsonProcessingException{
        //String email = authentication.getEmail();
        String userId = "a01054149478@gmail.com";
        String userMessagesListApi = String.format(GlobalConstant.GMAIL_USER_MESSAGES_LIST_API_FORMAT, userId, MESSAGES_MAX_RESULTS);
        // set object about http
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
        // get user messages list
        ResponseEntity<String> responseUserMessagesList = requestUserEmailMessagesList(userMessagesListApi, entity);
        if(responseUserMessagesList.getStatusCode() != HttpStatus.OK){

        }
        UserEmailMessagesList userEmailMessagesList = getUserEmailMessagesList(responseUserMessagesList);
        List<UserEmailMessagesListData> messages = userEmailMessagesList.getMessages();
        List<CompletableFuture<UserEmailMessagesListConvertedData>> futures = messages.stream()
                .map(message -> asyncEmailService.getUserEmailMessagesListConverted(userId, entity, message)
                        .thenApply(result -> {
                            System.out.println(Thread.currentThread().getName() + "seolhuigwan");
                            System.out.println(result);
                            return result;
                        })
                        .exceptionally(error -> {
                            System.out.println(error.getMessage());
                            return null;
                        })
                ).toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        System.out.println("waiting ....");
    }

    public void syncGetEmailMessages(String accessToken) throws JsonProcessingException{
        //String email = authentication.getEmail();
        String userId = "a01054149478@gmail.com";
        String userMessagesListApi = String.format(GlobalConstant.GMAIL_USER_MESSAGES_LIST_API_FORMAT, userId, MESSAGES_MAX_RESULTS);
        // set object about http
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
        // get user messages list
        ResponseEntity<String> responseUserMessagesList = requestUserEmailMessagesList(userMessagesListApi, entity);
        if(responseUserMessagesList.getStatusCode() != HttpStatus.OK){

        }
        UserEmailMessagesList userEmailMessagesList = getUserEmailMessagesList(responseUserMessagesList);
        List<UserEmailMessagesListData> messages = userEmailMessagesList.getMessages();
        messages.forEach((message) -> {
            UserEmailMessagesListConvertedData userEmailMessagesListConvertedData = asyncEmailService.syncGetUserEmailMessagesListConverted(
                    userId,
                    entity,
                    message
            );
            System.out.println(Thread.currentThread().getName());
            System.out.println(userEmailMessagesListConvertedData);
        });
    }

    private ResponseEntity<String> requestUserEmailMessagesList(String uri, HttpEntity<MultiValueMap<String, String>> entity){
        return restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
    }

    private UserEmailMessagesList getUserEmailMessagesList(ResponseEntity<String> entity) throws JsonProcessingException {
        return om.readValue(entity.getBody(), UserEmailMessagesList.class);
    }
}
