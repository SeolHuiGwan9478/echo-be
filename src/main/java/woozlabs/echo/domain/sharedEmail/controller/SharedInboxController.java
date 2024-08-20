package woozlabs.echo.domain.sharedEmail.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.sharedEmail.dto.ShareEmailRequestDto;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;
import woozlabs.echo.domain.sharedEmail.entity.Thread;
import woozlabs.echo.domain.sharedEmail.service.SharedInboxService;
import woozlabs.echo.global.constant.GlobalConstant;

import java.util.List;

@RestController
@RequestMapping("/api/v1/echo")
@RequiredArgsConstructor
public class SharedInboxController {

    private final SharedInboxService sharedInboxService;

    @GetMapping("/emails/team/{teamId}")
    public ResponseEntity<List<SharedEmail>> getSharedEmailsByTeam(HttpServletRequest httpServletRequest,
                                                                   @PathVariable("teamId") String teamId) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        List<SharedEmail> allEmails = sharedInboxService.getSharedEmailsByTeam(uid, teamId);
        return ResponseEntity.ok(allEmails);
    }

    @GetMapping("/thread/{threadId}")
    public ResponseEntity<Thread> getSharedThread(HttpServletRequest httpServletRequest,
                                                  @PathVariable("threadId") String threadId) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        Thread thread = sharedInboxService.getSharedThread(uid, threadId);
        if (thread == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(thread);
    }

    @PostMapping("/thread/share")
    public ResponseEntity<Void> shareEmail(HttpServletRequest httpServletRequest,
                                           @RequestBody ShareEmailRequestDto shareEmailRequestDto) {
        String sharedById = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        sharedInboxService.shareEmail(sharedById, shareEmailRequestDto);
        return ResponseEntity.status(201).build();
    }
}
