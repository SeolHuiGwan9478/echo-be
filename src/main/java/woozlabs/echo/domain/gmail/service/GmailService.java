package woozlabs.echo.domain.gmail.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Thread;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.gmail.exception.GmailException;
import woozlabs.echo.domain.gmail.dto.GmailThreadListResponse;
import woozlabs.echo.domain.gmail.dto.GmailThreadListThreads;
import woozlabs.echo.global.constant.GlobalConstant;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static woozlabs.echo.global.constant.GlobalConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GmailService {
    // constants
    private final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"
    );
    // injection & init
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final AsyncGmailService asyncGmailService;

    public GmailThreadListResponse getUserEmailThreads(String accessToken, String pageToken, String category) throws Exception{
        Gmail gmailService = createGmailService(accessToken);
        ListThreadsResponse response = getListThreadsResponse(pageToken, category, gmailService);
        List<Thread> threads = response.getThreads(); // get threads
        List<GmailThreadListThreads> detailedThreads = getDetailedThreads(threads, gmailService); // get detailed threads
        return GmailThreadListResponse.builder()
                .threads(detailedThreads)
                .nextPageToken(response.getNextPageToken())
                .build();
    }

    private List<GmailThreadListThreads> getDetailedThreads(List<Thread> threads, Gmail gmailService) {
        List<CompletableFuture<Optional<GmailThreadListThreads>>> futures = threads.stream()
                .map((thread) -> asyncGmailService.asyncRequestGmailThreadGetForList(thread, gmailService)
                        .thenApply(Optional::of)
                        .exceptionally(error -> {
                            log.error(error.getMessage());
                            return Optional.empty();
                        })
                ).toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // only get first top of message
        return futures.stream().map((future) -> {
            try{
                Optional<GmailThreadListThreads> result = future.get();
                if(result.isEmpty()) throw new GmailException(GlobalConstant.REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG);
                return result.get();
            }catch (InterruptedException | CancellationException | ExecutionException e){
                throw new GmailException(GlobalConstant.REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG);
            }
        }).toList();
    }

    private ListThreadsResponse getListThreadsResponse(String pageToken, String category, Gmail gmailService) throws IOException {
        return gmailService.users().threads()
                .list(USER_ID)
                .setMaxResults(THREADS_LIST_MAX_LENGTH)
                .setPageToken(pageToken)
                .setPrettyPrint(Boolean.TRUE)
                .setQ(THREADS_LIST_Q + SPACE_CHAR + category)
                .execute();
    }

    private HttpRequestInitializer createCredentialWithAccessToken(String accessToken){
        AccessToken token = AccessToken.newBuilder()
                .setTokenValue(accessToken)
                .setScopes(SCOPES)
                .build();
        GoogleCredentials googleCredentials = GoogleCredentials.create(token);
        return new HttpCredentialsAdapter(googleCredentials);
    }

    private Gmail createGmailService(String accessToken) throws Exception{
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        HttpRequestInitializer requestInitializer = createCredentialWithAccessToken(accessToken);
        return new Gmail.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .build();
    }
}