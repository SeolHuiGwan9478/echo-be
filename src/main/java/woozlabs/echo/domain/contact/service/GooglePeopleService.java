package woozlabs.echo.domain.contact.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListOtherContactsResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.contact.dto.GoogleContactResponseDto;
import woozlabs.echo.domain.member.repository.AccountRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GooglePeopleService {

    private static final String APPLICATION_NAME = "Echo";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final AccountRepository accountRepository;

    private PeopleService createPeopleService(String accessToken) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));

        return new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<GoogleContactResponseDto> getOtherContacts(String activeAccountUid) throws IOException, GeneralSecurityException {
        String accessToken = accountRepository.findAccessTokenByUid(activeAccountUid);
        PeopleService peopleService = createPeopleService(accessToken);

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
