package woozlabs.echo.domain.calendar.controller;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.calendar.dto.EventRequestDto;
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

    private static EventDateTime createEventDateTime(String dateTime, String timeZone) {
        return new EventDateTime()
                .setDateTime(new DateTime(dateTime))
                .setTimeZone(timeZone);
    }

    private static ConferenceData createGoogleMeetConferenceData() {
        CreateConferenceRequest conferenceRequest = new CreateConferenceRequest();
        conferenceRequest.setRequestId("randomString-" + System.currentTimeMillis());
        ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey();
        conferenceSolutionKey.setType("hangoutsMeet");
        conferenceRequest.setConferenceSolutionKey(conferenceSolutionKey);

        return new ConferenceData().setCreateRequest(conferenceRequest);
    }

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
    public ResponseEntity<Event> createEvent(@RequestParam("accessToken") String accessToken,
                                              @RequestBody EventRequestDto requestDto) {
        Event createEvent = null;
        try {
            Event event = new Event()
                    .setSummary(requestDto.getSummary())
                    .setLocation(requestDto.getLocation())
                    .setDescription(requestDto.getDescription())
                    .setStart(createEventDateTime(requestDto.getStartDateTime(), requestDto.getTimeZone()))
                    .setEnd(createEventDateTime(requestDto.getEndDateTime(), requestDto.getTimeZone()));

            if (requestDto.isCreateGoogleMeet()) {
                event.setConferenceData(createGoogleMeetConferenceData());
                createEvent = calendarService.createEventWithConference(accessToken, event);
            } else {
                createEvent = calendarService.createEvent(accessToken, event);
            }
        } catch (GeneralSecurityException e) {
            log.error("Security error while fetching Google Calendar events", e);
            throw new CustomErrorException(ErrorCode.GOOGLE_CALENDAR_SECURITY_ERROR);
        } catch (IOException e) {
            log.error("IO error while posting Google Calendar events", e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_POST_GOOGLE_CALENDAR);
        }
        return ResponseEntity.ok(createEvent);
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<Event> updateEvent(@RequestParam("accessToken") String accessToken,
                                             @PathVariable("eventId") String eventId,
                                             @RequestBody EventRequestDto requestDto) {

        try {
            Event event = new Event()
                    .setSummary(requestDto.getSummary())
                    .setLocation(requestDto.getLocation())
                    .setDescription(requestDto.getDescription())
                    .setStart(createEventDateTime(requestDto.getStartDateTime(), requestDto.getTimeZone()))
                    .setEnd(createEventDateTime(requestDto.getEndDateTime(), requestDto.getTimeZone()));

            if (requestDto.isCreateGoogleMeet()) {
                event.setConferenceData(createGoogleMeetConferenceData());
            }

            Event updatedEvent = calendarService.updateEvent(accessToken, eventId, event);
            return ResponseEntity.ok(updatedEvent);
        } catch (GeneralSecurityException e) {
            log.error("Security error while updating Google Calendar event", e);
            throw new CustomErrorException(ErrorCode.GOOGLE_CALENDAR_SECURITY_ERROR);
        } catch (IOException e) {
            log.error("IO error while updating Google Calendar event", e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_UPDATE_GOOGLE_CALENDAR);
        }
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(@RequestParam("accessToken") String accessToken,
                                            @PathVariable("eventId") String eventId) {
        try {
            calendarService.deleteEvent(accessToken, eventId);
            return ResponseEntity.noContent().build();
        } catch (GeneralSecurityException e) {
            log.error("Security error while deleting Google Calendar event", e);
            throw new CustomErrorException(ErrorCode.GOOGLE_CALENDAR_SECURITY_ERROR);
        } catch (IOException e) {
            log.error("IO error while deleting Google Calendar event", e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_DELETE_GOOGLE_CALENDAR);
        }
    }
}
