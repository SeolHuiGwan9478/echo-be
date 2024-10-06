package woozlabs.echo.domain.contact.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.ListOtherContactsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.contact.dto.GoogleContactResponseDto;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GooglePeopleService {

    private static final String APPLICATION_NAME = "Echo";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Arrays.asList(PeopleServiceScopes.CONTACTS_OTHER_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private final PeopleService peopleService;

    public GooglePeopleService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.peopleService = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = GooglePeopleService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public List<GoogleContactResponseDto> getOtherContacts() throws IOException {
        ListOtherContactsResponse response = peopleService.otherContacts()
                .list()
                .setPageSize(30)
                .setReadMask("names,emailAddresses,photos")
                .execute();

        return response.getOtherContacts().stream()
                .map(person -> {
                    String email = (person.getEmailAddresses() != null && !person.getEmailAddresses().isEmpty())
                            ? person.getEmailAddresses().get(0).getValue()
                            : null;

                    String displayName = (person.getNames() != null && !person.getNames().isEmpty())
                            ? person.getNames().get(0).getDisplayName()
                            : null;

                    String familyName = (person.getNames() != null && !person.getNames().isEmpty())
                            ? person.getNames().get(0).getFamilyName()
                            : null;

                    String givenName = (person.getNames() != null && !person.getNames().isEmpty())
                            ? person.getNames().get(0).getGivenName()
                            : null;

                    String profileImageUrl = (person.getPhotos() != null && !person.getPhotos().isEmpty())
                            ? person.getPhotos().get(0).getUrl()
                            : null;

                    return GoogleContactResponseDto.builder()
                            .email(email)
                            .names(GoogleContactResponseDto.Name.builder()
                                    .displayName(displayName)
                                    .familyName(familyName)
                                    .givenName(givenName)
                                    .build())
                            .profileImageUrl(profileImageUrl)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
