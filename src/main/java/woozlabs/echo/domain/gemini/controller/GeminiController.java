package woozlabs.echo.domain.gemini.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.gemini.service.GeminiService;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetResponse;
import woozlabs.echo.domain.gmail.service.GmailService;
import woozlabs.echo.global.constant.GlobalConstant;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/gemini")
public class GeminiController {

    private final GeminiService geminiService;
    private final GmailService gmailService;

    @PostMapping("/completion")
    public ResponseEntity<String> getCompletion(@RequestBody String text) {
        String completion = geminiService.getCompletion(text);
        return ResponseEntity.ok(completion);
    }

    @PostMapping("/thread-summarize/{threadId}")
    public ResponseEntity<String> summarizeGmailThread(HttpServletRequest httpServletRequest, @PathVariable("threadId") String threadId) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        try {
            GmailThreadGetResponse gmailThread = gmailService.getUserEmailThread(uid, threadId);
            String summary = geminiService.summarizeGmailThread(gmailThread);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error summarizing Gmail thread: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
