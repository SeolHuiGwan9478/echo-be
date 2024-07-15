package woozlabs.echo.domain.calendar.controller;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.calendar.dto.EventRequestDto;
import woozlabs.echo.domain.calendar.service.CalendarService;
import woozlabs.echo.global.constant.GlobalConstant;
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
    public ResponseEntity<List<Event>> getEvents(HttpServletRequest httpServletRequest) {
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            List<Event> events = calendarService.getEvents(uid);
            return ResponseEntity.ok(events);
        } catch (GeneralSecurityException e) {
            log.error("Security error while fetching Google Calendar events", e);
            throw new CustomErrorException(ErrorCode.GOOGLE_CALENDAR_SECURITY_ERROR, e.getMessage());
        } catch (IOException e) {
            log.error("IO error while fetching Google Calendar events", e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_FETCH_GOOGLE_CALENDAR, e.getMessage());
        }
    }

    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(HttpServletRequest httpServletRequest,
                                              @RequestBody EventRequestDto requestDto) {
        Event createEvent = null;
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            Event event = new Event()
                    .setSummary(requestDto.getSummary())
                    .setLocation(requestDto.getLocation())
                    .setDescription(requestDto.getDescription())
                    .setStart(createEventDateTime(requestDto.getStartDateTime(), requestDto.getTimeZone()))
                    .setEnd(createEventDateTime(requestDto.getEndDateTime(), requestDto.getTimeZone()));

            if (requestDto.isCreateGoogleMeet()) {
                event.setConferenceData(createGoogleMeetConferenceData());
                createEvent = calendarService.createEventWithConference(uid, event);
            } else {
                createEvent = calendarService.createEvent(uid, event);
            }
        } catch (GeneralSecurityException e) {
            log.error("Security error while fetching Google Calendar events", e);
            throw new CustomErrorException(ErrorCode.GOOGLE_CALENDAR_SECURITY_ERROR, e.getMessage());
        } catch (IOException e) {
            log.error("IO error while posting Google Calendar events", e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_POST_GOOGLE_CALENDAR, e.getMessage());
        }
        return ResponseEntity.ok(createEvent);
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<Event> updateEvent(HttpServletRequest httpServletRequest,
                                             @PathVariable("eventId") String eventId,
                                             @RequestBody EventRequestDto requestDto) {

        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            Event event = new Event()
                    .setSummary(requestDto.getSummary())
                    .setLocation(requestDto.getLocation())
                    .setDescription(requestDto.getDescription())
                    .setStart(createEventDateTime(requestDto.getStartDateTime(), requestDto.getTimeZone()))
                    .setEnd(createEventDateTime(requestDto.getEndDateTime(), requestDto.getTimeZone()));

            if (requestDto.isCreateGoogleMeet()) {
                event.setConferenceData(createGoogleMeetConferenceData());
            }

            Event updatedEvent = calendarService.updateEvent(uid, eventId, event);
            return ResponseEntity.ok(updatedEvent);
        } catch (GeneralSecurityException e) {
            log.error("Security error while updating Google Calendar event", e);
            throw new CustomErrorException(ErrorCode.GOOGLE_CALENDAR_SECURITY_ERROR, e.getMessage());
        } catch (IOException e) {
            log.error("IO error while updating Google Calendar event", e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_UPDATE_GOOGLE_CALENDAR, e.getMessage());
        }
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(HttpServletRequest httpServletRequest,
                                            @PathVariable("eventId") String eventId) {
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            calendarService.deleteEvent(uid, eventId);
            return ResponseEntity.noContent().build();
        } catch (GeneralSecurityException e) {
            log.error("Security error while deleting Google Calendar event", e);
            throw new CustomErrorException(ErrorCode.GOOGLE_CALENDAR_SECURITY_ERROR, e.getMessage());
        } catch (IOException e) {
            log.error("IO error while deleting Google Calendar event", e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_DELETE_GOOGLE_CALENDAR, e.getMessage());
        }
    }
}
