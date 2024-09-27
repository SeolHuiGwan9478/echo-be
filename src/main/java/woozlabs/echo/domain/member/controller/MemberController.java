package woozlabs.echo.domain.member.controller;

import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.member.dto.ChangePrimaryAccountRequestDto;
import woozlabs.echo.domain.member.dto.ChangePrimaryAccountResponseDto;
import woozlabs.echo.domain.member.dto.GetPrimaryAccountResponseDto;
import woozlabs.echo.domain.member.dto.preference.PreferenceDto;
import woozlabs.echo.domain.member.dto.preference.UpdatePreferenceRequestDto;
import woozlabs.echo.domain.member.dto.profile.ChangeProfileRequestDto;
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
    public ResponseEntity<Void> softDeleteMember(@PathVariable("uid") String uid) {
        memberService.softDeleteMember(uid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{uid}/super-hard-delete")
    public ResponseEntity<Void> superHardDeleteMember(@PathVariable("uid") String uid) {
        memberService.superHardDeleteMember(uid);
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

    @PatchMapping("/{uid}/profile")
    public ResponseEntity<Void> changeProfile(@PathVariable("uid") String primaryUid,
                                              @RequestBody ChangeProfileRequestDto changeProfileRequestDto) {
        memberService.changeProfile(primaryUid, changeProfileRequestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{uid}/change-primary-account")
    public ResponseEntity<ChangePrimaryAccountResponseDto> changePrimaryAccount(@PathVariable("uid") String primaryUid,
                                                                                @RequestBody ChangePrimaryAccountRequestDto changePrimaryAccountRequestDto) throws FirebaseAuthException {
        ChangePrimaryAccountResponseDto responseDto = memberService.changePrimaryAccount(primaryUid, changePrimaryAccountRequestDto.getNewPrimaryUid());
        return ResponseEntity.ok(responseDto);
    }
}
