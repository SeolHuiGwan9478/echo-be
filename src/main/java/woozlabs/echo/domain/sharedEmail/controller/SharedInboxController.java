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
    public ResponseEntity<SharedEmailResponseDto> createSharePost(@RequestParam("aAUid") String activeAccountUid,
                                                                  @RequestBody CreateSharedRequestDto createSharedRequestDto) {
        SharedEmailResponseDto responseDto = sharedInboxService.createSharePost(activeAccountUid, createSharedRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/public-share/{sharedEmailId}/invite")
    public ResponseEntity<SharedEmailResponseDto> inviteToSharedPost(@RequestParam("aAUid") String activeAccountUid,
                                                                     @PathVariable("sharedEmailId") UUID sharedEmailId,
                                                                     @RequestBody SendSharedEmailInvitationDto sendSharedEmailInvitationDto) {
        SharedEmailResponseDto sharedEmail = sharedInboxService.inviteToSharedPost(activeAccountUid, sharedEmailId, sendSharedEmailInvitationDto);
        return ResponseEntity.ok(sharedEmail);
    }

    @PatchMapping("/public-share/{sharedEmailId}/update")
    public ResponseEntity<SharedEmailResponseDto> updateSharedPost(@RequestParam("aAUid") String activeAccountUid,
                                                                   @PathVariable("sharedEmailId") UUID sharedEmailId,
                                                                   @RequestBody UpdateSharedPostDto updateSharedPostDto) {
        SharedEmailResponseDto updatedSharedEmail = sharedInboxService.updateSharedPost(activeAccountUid, sharedEmailId, updateSharedPostDto);
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
    public ResponseEntity<UpdateInviteePermissionsDto> updateInviteePermissions(@RequestParam("aAUid") String activeAccountUid,
                                                                                @PathVariable("sharedEmailId") UUID sharedEmailId,
                                                                                @RequestBody UpdateInviteePermissionsDto updateInviteePermissionsDto) {
        UpdateInviteePermissionsDto responseDto = sharedInboxService.updateInviteePermissions(activeAccountUid, sharedEmailId, updateInviteePermissionsDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/public-share/{sharedEmailId}/exclude-permissions")
    public ResponseEntity<SharedEmailResponseDto> excludeInvitees(@RequestParam("aAUid") String activeAccountUid,
                                                                  @PathVariable("sharedEmailId") UUID sharedEmailId,
                                                                  @RequestBody ExcludeInviteesRequestDto excludeInviteesRequestDto) {
        SharedEmailResponseDto responseDto = sharedInboxService.excludeInvitees(activeAccountUid, sharedEmailId, excludeInviteesRequestDto);
        return ResponseEntity.ok(responseDto);
    }
}
