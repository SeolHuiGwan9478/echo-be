package woozlabs.echo.global.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.Map;

@Component
public class GoogleOAuthUtils {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    public Map<String, Object> getGoogleUserInfo(String accessToken) {
        String userInfoEndpointUri = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;

        ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoEndpointUri, Map.class);
        if (userInfoResponse.getStatusCode().is2xxSuccessful()) {
            return userInfoResponse.getBody();
        } else {
            throw new CustomErrorException(ErrorCode.FAILED_TO_FETCH_GOOGLE_USER_INFO_UTILS);
        }
    }

    public Map<String, String> getGoogleTokens(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("code", code);
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("redirect_uri", redirectUri);
        requestBody.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return responseEntity.getBody();
    }

    public Map<String, String> refreshAccessToken(String refreshToken) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("refresh_token", refreshToken);
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return responseEntity.getBody();
    }
}
