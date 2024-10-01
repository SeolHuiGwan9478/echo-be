package woozlabs.echo.domain.calendar.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.calendar.dto.CalendarListResponse;
import woozlabs.echo.domain.calendar.dto.UnAvailableDatesResponse;
import woozlabs.echo.domain.calendar.service.CalendarService;
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.global.constant.GlobalConstant;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;
    private final GmailUtility gmailUtility;

    @GetMapping("/api/v1/calendar/calendars")
    public ResponseEntity<?> getCalendars(HttpServletRequest httpServletRequest){
        log.info("Request to get own calendars");
        String aAUid = gmailUtility.getActiveAccountUid(httpServletRequest);
        CalendarListResponse response = calendarService.getCalendars(aAUid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/v1/calendar/events/unavailable")
    public ResponseEntity<?> getUnavailableEvents(HttpServletRequest httpServletRequest) {
        log.info("Request to get unavailable events");
        String aAUid = gmailUtility.getActiveAccountUid(httpServletRequest);
        UnAvailableDatesResponse response = calendarService.getDatesWithNoEventsInTwoWeeks(aAUid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}