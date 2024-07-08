package woozlabs.echo.domain.calendar.controller;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.calendar.dto.CreateEventRequestDto;
import woozlabs.echo.domain.calendar.service.CalendarService;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/events")
    public ResponseEntity<List<Event>> getEvents(@RequestParam("accessToken") String accessToken) {
        try {
            List<Event> events = calendarService.getEvents(accessToken);
            return ResponseEntity.ok(events);
        } catch (GeneralSecurityException e) {
            log.error("Security error while fetching Google Calendar events", e);
            throw new CustomErrorException(ErrorCode.GOOGLE_CALENDAR_SECURITY_ERROR);
        } catch (IOException e) {
            log.error("IO error while fetching Google Calendar events", e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_FETCH_GOOGLE_CALENDAR);
        }
    }

    @PostMapping("/events")
    public ResponseEntity<Event> createEvents(@RequestParam("accessToken") String accessToken,
                                              @RequestBody CreateEventRequestDto requestDto) {
        Event createEvent = null;
        try {
            Event event = new Event()
                    .setSummary(requestDto.getSummary())
                    .setLocation(requestDto.getLocation())
                    .setDescription(requestDto.getDescription())
                    // Assuming startDateTime and endDateTime are ISO 8601 formatted strings
                    .setStart(new EventDateTime().setDateTime(new DateTime(requestDto.getStartDateTime())))
                    .setEnd(new EventDateTime().setDateTime(new DateTime(requestDto.getEndDateTime())));

            createEvent = calendarService.createEvent(accessToken, event);
        } catch (GeneralSecurityException e) {
            log.error("Security error while fetching Google Calendar events", e);
            throw new CustomErrorException(ErrorCode.GOOGLE_CALENDAR_SECURITY_ERROR);
        } catch (IOException e) {
            log.error("IO error while posting Google Calendar events", e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_POST_GOOGLE_CALENDAR);
        }
        return ResponseEntity.ok(createEvent);
    }

}
