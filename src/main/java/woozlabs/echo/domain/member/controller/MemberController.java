package woozlabs.echo.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.member.dto.GetPrimaryAccountResponseDto;
import woozlabs.echo.domain.member.dto.PreferenceDto;
import woozlabs.echo.domain.member.dto.UpdatePreferenceRequestDto;
import woozlabs.echo.domain.member.service.MemberService;

@RestController
@RequestMapping("/api/v1/echo/user")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{uid}/preferences")
    public ResponseEntity<PreferenceDto> getPreferences(@PathVariable("uid") String uid) {
        PreferenceDto preference = memberService.getPreference(uid);
        return ResponseEntity.ok(preference);
    }

    @PatchMapping("/{uid}/preferences")
    public ResponseEntity<Void> updatePreferences(@PathVariable("uid") String uid, @RequestBody UpdatePreferenceRequestDto updatePreferenceRequest) {
        memberService.updatePreference(uid, updatePreferenceRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{uid}/delete")
    public ResponseEntity<Void> deleteMember(@PathVariable("uid") String uid) {
        memberService.deleteMember(uid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{uid}/hard-delete")
    public ResponseEntity<Void> hardDeleteMember(@PathVariable("uid") String uid) {
        memberService.hardDeleteMember(uid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/account-info/{uid}")
    public ResponseEntity<?> getAccountInfo(@PathVariable("uid") String uid) {
        Object response = memberService.getAccountInfo(uid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<GetPrimaryAccountResponseDto> createMember(@RequestParam String uid) {
        GetPrimaryAccountResponseDto responseDto = memberService.createMember(uid);
        return ResponseEntity.ok(responseDto);
    }
}
