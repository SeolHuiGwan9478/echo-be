package woozlabs.echo.domain.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.member.dto.MemberDto;
import woozlabs.echo.domain.member.dto.ProfileResponseDto;
import woozlabs.echo.domain.member.service.MemberService;
import woozlabs.echo.global.constant.GlobalConstant;

@RestController
@RequestMapping("/api/v1/echo/user")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDto> getProfileByEmail(@RequestParam String email) {
        ProfileResponseDto response = memberService.getProfileByField("email", email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uid}/profile")
    public ResponseEntity<ProfileResponseDto> getProfileByUid(@PathVariable("uid") String uid) {
        ProfileResponseDto response = memberService.getProfileByField("uid", uid);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<MemberDto> getMemberWithLinkedAccounts(HttpServletRequest httpServletRequest) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        MemberDto memberDto = memberService.getMemberWithLinkedAccounts(uid);
        return ResponseEntity.ok(memberDto);
    }
}
