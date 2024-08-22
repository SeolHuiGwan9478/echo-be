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
import woozlabs.echo.global.constant.GlobalConstant;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @GetMapping("/api/v1/calendar/calendars")
    public ResponseEntity<?> getCalendars(HttpServletRequest httpServletRequest){
        log.info("Request to get own calendars");
        try{
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            CalendarListResponse response = calendarService.getCalendars(uid);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (GeneralSecurityException | IOException e){
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/v1/calendar/events/unavailable")
    public ResponseEntity<?> getUnavailableEvents(HttpServletRequest httpServletRequest) {
        log.info("Request to get unavailable events");
        try {
            String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
            UnAvailableDatesResponse response = calendarService.getDatesWithNoEventsInTwoWeeks(uid);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (GeneralSecurityException | IOException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}