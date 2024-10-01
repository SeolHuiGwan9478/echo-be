package woozlabs.echo.domain.calendar.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.calendar.dto.CalendarListData;
import woozlabs.echo.domain.calendar.dto.CalendarListResponse;
import woozlabs.echo.domain.calendar.dto.UnAvailableDatesResponse;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
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
    private final String PRIMARY_CALENDAR_ID = "primary";
    private final String DATES_CONNECTION_CHAR = " ~ ";
    private final AccountRepository accountRepository;

    public CalendarListResponse getCalendars(String uid){
        try{
            Calendar calendarService = getCalendarService(uid);
            CalendarList calendarList = calendarService.calendarList().list().execute();
            List<CalendarListEntry> calendarListEntries = calendarList.getItems();
            List<CalendarListData> calendarListData = calendarListEntries.stream()
                    .map(CalendarListData::toCalendarListData).toList();
            return CalendarListResponse.builder()
                    .totalCounts(calendarListData.size())
                    .data(calendarListData)
                    .build();
        }catch (GeneralSecurityException | IOException e) {
            throw new CustomErrorException(ErrorCode.CALENDAR_SERVICE_ERROR_MESSAGE, ErrorCode.CALENDAR_SERVICE_ERROR_MESSAGE.getMessage());
        }
    }

    public UnAvailableDatesResponse getDatesWithNoEventsInTwoWeeks(String uid){
        try{
            Calendar calendarService = getCalendarService(uid);
            DateTime today = new DateTime(System.currentTimeMillis());
            DateTime twoWeeksLater = new DateTime(System.currentTimeMillis() + 14L * 24L * 60L * 60L * 1000L);
            Events eventsInTwoWeeks = calendarService.events()
                    .list(PRIMARY_CALENDAR_ID)
                    .setTimeMin(today)
                    .setTimeMax(twoWeeksLater)
                    .setSingleEvents(true)
                    .execute();
            List<Event> events = eventsInTwoWeeks.getItems();
            List<String> unAvailableDates = events.stream().map((event) -> {
                DateTime startDateTime = event.getStart().getDateTime();
                DateTime endDateTime = event.getEnd().getDateTime();
                return startDateTime + DATES_CONNECTION_CHAR + endDateTime;
            }).toList();
            return UnAvailableDatesResponse.builder()
                    .unavailableDates(unAvailableDates)
                    .build();
        }catch (GeneralSecurityException | IOException e) {
            throw new CustomErrorException(ErrorCode.CALENDAR_SERVICE_ERROR_MESSAGE, ErrorCode.CALENDAR_SERVICE_ERROR_MESSAGE.getMessage());
        }
    }

    private Calendar getCalendarService(String uid) throws GeneralSecurityException, IOException {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null))
                .createScoped(Collections.singleton(CALENDAR_SCOPE));
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Echo")
                .build();
    }
}
