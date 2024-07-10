package woozlabs.echo.domain.calendar.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";
    private final MemberRepository memberRepository;

    private Calendar getCalendarService(String uid) throws GeneralSecurityException, IOException {
        Member member = memberRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        String accessToken = member.getAccessToken();

        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null))
                .createScoped(Collections.singleton(CALENDAR_SCOPE));

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Echo")
                .build();
    }

    public List<Event> getEvents(String uid) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(uid);
        com.google.api.client.util.DateTime now = new com.google.api.client.util.DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems();
    }

    public Event createEvent(String uid, Event event) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(uid);
        return service.events().insert("primary", event).execute();
    }

    public Event createEventWithConference(String uid, Event event) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(uid);
        return service.events().insert("primary", event)
                .setConferenceDataVersion(1)
                .execute();
    }

    public Event updateEvent(String uid, String eventId, Event event) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(uid);
        return service.events().update("primary", eventId, event).execute();
    }

    public void deleteEvent(String uid, String eventId) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(uid);
        service.events().delete("primary", eventId).execute();
    }
}
