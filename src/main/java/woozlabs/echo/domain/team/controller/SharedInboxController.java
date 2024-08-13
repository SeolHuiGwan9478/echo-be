package woozlabs.echo.domain.team.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.team.dto.ShareEmailRequestDto;
import woozlabs.echo.domain.team.entity.SharedEmail;
import woozlabs.echo.domain.team.service.SharedInboxService;
import woozlabs.echo.global.constant.GlobalConstant;

import java.util.List;

@RestController
@RequestMapping("/api/v1/echo")
@RequiredArgsConstructor
public class SharedInboxController {

    private final SharedInboxService sharedInboxService;

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<SharedEmail>> getSharedEmailsByTeam(@PathVariable("teamId") String teamId) {
        List<SharedEmail> allEmails = sharedInboxService.getSharedEmailsByTeam(teamId);
        return ResponseEntity.ok(allEmails);
    }

    @PostMapping("/share")
    public ResponseEntity<Void> shareEmail(HttpServletRequest httpServletRequest,
                                                  @RequestBody ShareEmailRequestDto shareEmailRequestDto) {
        String sharedById = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        sharedInboxService.shareEmail(sharedById, shareEmailRequestDto);
        return ResponseEntity.status(201).build();
    }
}
