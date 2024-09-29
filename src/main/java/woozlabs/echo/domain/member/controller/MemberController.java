package woozlabs.echo.domain.member.controller;

import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletRequest;
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
import woozlabs.echo.global.constant.GlobalConstant;

@RestController
@RequestMapping("/api/v1/echo/user")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/preferences")
    public ResponseEntity<PreferenceDto> getPreferences(HttpServletRequest httpServletRequest) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        PreferenceDto preference = memberService.getPreference(uid);
        return ResponseEntity.ok(preference);
    }

    @PatchMapping("/preferences")
    public ResponseEntity<Void> updatePreferences(HttpServletRequest httpServletRequest,
                                                  @RequestBody UpdatePreferenceRequestDto updatePreferenceRequest) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        memberService.updatePreference(uid, updatePreferenceRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> softDeleteMember(HttpServletRequest httpServletRequest) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        memberService.softDeleteMember(uid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/super-hard-delete")
    public ResponseEntity<Void> superHardDeleteMember(HttpServletRequest httpServletRequest) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        memberService.superHardDeleteMember(uid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/account-info")
    public ResponseEntity<Object> getAccountInfo(HttpServletRequest httpServletRequest) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        Object response = memberService.getAccountInfo(uid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<GetPrimaryAccountResponseDto> createMember(HttpServletRequest httpServletRequest) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        GetPrimaryAccountResponseDto responseDto = memberService.createMember(uid);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/change-profile")
    public ResponseEntity<Void> changeProfile(HttpServletRequest httpServletRequest,
                                              @RequestBody ChangeProfileRequestDto changeProfileRequestDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        memberService.changeProfile(uid, changeProfileRequestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-primary-account")
    public ResponseEntity<ChangePrimaryAccountResponseDto> changePrimaryAccount(HttpServletRequest httpServletRequest,
                                                                                @RequestBody ChangePrimaryAccountRequestDto changePrimaryAccountRequestDto) throws FirebaseAuthException {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        ChangePrimaryAccountResponseDto responseDto = memberService.changePrimaryAccount(uid, changePrimaryAccountRequestDto.getUid());
        return ResponseEntity.ok(responseDto);
    }
}
