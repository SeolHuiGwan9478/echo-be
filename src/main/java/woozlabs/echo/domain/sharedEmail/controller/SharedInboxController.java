package woozlabs.echo.domain.sharedEmail.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.sharedEmail.dto.*;
import woozlabs.echo.domain.sharedEmail.dto.thread.ThreadGetResponse;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;
import woozlabs.echo.domain.sharedEmail.service.SharedInboxService;
import woozlabs.echo.global.constant.GlobalConstant;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/echo")
@RequiredArgsConstructor
public class SharedInboxController {

    private final SharedInboxService sharedInboxService;

    @PostMapping("/public-share/create")
    public ResponseEntity<SharedEmail> createSharePost(HttpServletRequest httpServletRequest,
                                                       @RequestBody CreateSharedRequestDto createSharedRequestDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        SharedEmail sharedEmail = sharedInboxService.createSharePost(uid, createSharedRequestDto);
        return ResponseEntity.ok(sharedEmail);
    }

    @PostMapping("/public-share/{sharedEmailId}/invite")
    public ResponseEntity<SharedEmail> inviteToSharedPost(HttpServletRequest httpServletRequest,
                                                       @PathVariable("sharedEmailId") UUID sharedEmailId,
                                                       @RequestBody SendSharedEmailInvitationDto sendSharedEmailInvitationDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        SharedEmail sharedEmail = sharedInboxService.inviteToSharedPost(uid, sharedEmailId, sendSharedEmailInvitationDto);
        return ResponseEntity.ok(sharedEmail);
    }

    @PatchMapping("/public-share/{sharedEmailId}/update")
    public ResponseEntity<SharedEmail> updateSharedPost(HttpServletRequest httpServletRequest,
                                                        @PathVariable("sharedEmailId") UUID sharedEmailId,
                                                        @RequestBody UpdateSharedPostDto updateSharedPostDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        SharedEmail updatedSharedEmail = sharedInboxService.updateSharedPost(uid, sharedEmailId, updateSharedPostDto);
        return ResponseEntity.ok(updatedSharedEmail);
    }

    @PostMapping("/public-share")
    public ResponseEntity<ThreadGetResponse> shareEmail(HttpServletRequest httpServletRequest,
                                                        @RequestBody ShareEmailRequestDto shareEmailRequestDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        sharedInboxService.publicShareEmail(uid, shareEmailRequestDto);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/public-share/{dataId}")
    public ResponseEntity<GetSharedEmailResponseDto> getSharedEmail(HttpServletRequest httpServletRequest,
                                                                    @PathVariable("dataId") String dataId) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        GetSharedEmailResponseDto responseDto = sharedInboxService.getSharedEmail(uid, dataId);
        return ResponseEntity.ok(responseDto);
    }
}
