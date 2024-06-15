package woozlabs.echo.domain.email.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import woozlabs.echo.domain.email.dto.UserEmailMessagesListConvertedData;
import woozlabs.echo.domain.email.dto.UserEmailMessagesListData;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AsyncEmailService {
    private final RestTemplate restTemplate;
    private final ObjectMapper om;
    @Async
    public CompletableFuture<UserEmailMessagesListConvertedData> getUserEmailMessagesListConverted(String userId, HttpEntity<MultiValueMap<String, String>> entity, UserEmailMessagesListData userEmailMessagesListData){
        String id = userEmailMessagesListData.getId();
        String userMessagesGetApi = String.format(GlobalConstant.GMAIL_USER_MESSAGES_GET_API_FORMAT, userId, id);
        ResponseEntity<String> responseEntity = restTemplate.exchange(userMessagesGetApi, HttpMethod.GET, entity, String.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_MESSAGES_GET_API_ERROR_MESSAGE);
        }
        try {
            UserEmailMessagesListConvertedData data = om.readValue(responseEntity.getBody(), UserEmailMessagesListConvertedData.class);
            return CompletableFuture.completedFuture(data);
        } catch (JsonProcessingException e){
            throw new CustomErrorException(ErrorCode.OBJECT_MAPPER_JSON_PARSING_ERROR_MESSAGE);
        }
    }

    public UserEmailMessagesListConvertedData syncGetUserEmailMessagesListConverted(String userId, HttpEntity<MultiValueMap<String, String>> entity, UserEmailMessagesListData userEmailMessagesListData){
        String id = userEmailMessagesListData.getId();
        String userMessagesGetApi = String.format(GlobalConstant.GMAIL_USER_MESSAGES_GET_API_FORMAT, userId, id);
        ResponseEntity<String> responseEntity = restTemplate.exchange(userMessagesGetApi, HttpMethod.GET, entity, String.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK){
            throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_MESSAGES_GET_API_ERROR_MESSAGE);
        }
        try {
            return om.readValue(responseEntity.getBody(), UserEmailMessagesListConvertedData.class);
        } catch (JsonProcessingException e){
            throw new CustomErrorException(ErrorCode.OBJECT_MAPPER_JSON_PARSING_ERROR_MESSAGE);
        }
    }
}