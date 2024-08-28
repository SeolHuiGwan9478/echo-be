package woozlabs.echo.domain.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.member.dto.AccountDto;
import woozlabs.echo.domain.member.dto.ProfileResponseDto;
import woozlabs.echo.domain.member.dto.UpdatePreferenceRequestDto;
import woozlabs.echo.domain.member.service.AccountService;
import woozlabs.echo.domain.member.service.MemberService;
import woozlabs.echo.global.constant.GlobalConstant;

@RestController
@RequestMapping("/api/v1/echo/user")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDto> getProfileByEmail(@RequestParam String email) {
        ProfileResponseDto response = accountService.getProfileByField("email", email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uid}/profile")
    public ResponseEntity<ProfileResponseDto> getProfileByUid(@PathVariable("uid") String uid) {
        ProfileResponseDto response = accountService.getProfileByField("uid", uid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{uid}/preference")
    public ResponseEntity<Void> updatePreference(@PathVariable("uid") String uid, @RequestBody UpdatePreferenceRequestDto updatePreferenceRequestDto) {
        accountService.updatePreference(uid, updatePreferenceRequestDto);
        return ResponseEntity.ok().build();
    }
}
