package woozlabs.echo.domain.sharedEmail.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.sharedEmail.dto.*;
import woozlabs.echo.domain.sharedEmail.service.SharedInboxService;
import woozlabs.echo.global.constant.GlobalConstant;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/echo")
@RequiredArgsConstructor
public class SharedInboxController {

    private final SharedInboxService sharedInboxService;

    @PostMapping("/public-share/create")
    public ResponseEntity<SharedEmailResponseDto> createSharePost(HttpServletRequest httpServletRequest,
                                                       @RequestBody CreateSharedRequestDto createSharedRequestDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        SharedEmailResponseDto responseDto = sharedInboxService.createSharePost(uid, createSharedRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/public-share/{sharedEmailId}/invite")
    public ResponseEntity<SharedEmailResponseDto> inviteToSharedPost(HttpServletRequest httpServletRequest,
                                                       @PathVariable("sharedEmailId") UUID sharedEmailId,
                                                       @RequestBody SendSharedEmailInvitationDto sendSharedEmailInvitationDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        SharedEmailResponseDto sharedEmail = sharedInboxService.inviteToSharedPost(uid, sharedEmailId, sendSharedEmailInvitationDto);
        return ResponseEntity.ok(sharedEmail);
    }

    @PatchMapping("/public-share/{sharedEmailId}/update")
    public ResponseEntity<SharedEmailResponseDto> updateSharedPost(HttpServletRequest httpServletRequest,
                                                        @PathVariable("sharedEmailId") UUID sharedEmailId,
                                                        @RequestBody UpdateSharedPostDto updateSharedPostDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        SharedEmailResponseDto updatedSharedEmail = sharedInboxService.updateSharedPost(uid, sharedEmailId, updateSharedPostDto);
        return ResponseEntity.ok(updatedSharedEmail);
    }

    @GetMapping("/public-share/{sharedEmailId}")
    public ResponseEntity<GetSharedEmailResponseDto> getSharedEmail(HttpServletRequest httpServletRequest,
                                                                    @PathVariable("sharedEmailId") UUID sharedEmailId) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        GetSharedEmailResponseDto responseDto = sharedInboxService.getSharedEmail(uid, sharedEmailId);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/public-share/{sharedEmailId}/update-permissions")
    public ResponseEntity<UpdateInviteePermissionsDto> updateInviteePermissions(HttpServletRequest httpServletRequest,
                                                                @PathVariable("sharedEmailId") UUID sharedEmailId,
                                                                @RequestBody UpdateInviteePermissionsDto updateInviteePermissionsDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        UpdateInviteePermissionsDto responseDto = sharedInboxService.updateInviteePermissions(uid, sharedEmailId, updateInviteePermissionsDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/public-share/{sharedEmailId}/exclude-permissions")
    public ResponseEntity<SharedEmailResponseDto> excludeInvitees(HttpServletRequest httpServletRequest,
                                                                  @PathVariable("sharedEmailId") UUID sharedEmailId,
                                                                  @RequestBody ExcludeInviteesRequestDto excludeInviteesRequestDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        SharedEmailResponseDto responseDto = sharedInboxService.excludeInvitees(uid, sharedEmailId, excludeInviteesRequestDto);
        return ResponseEntity.ok(responseDto);
    }
}
