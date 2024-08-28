package woozlabs.echo.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.member.dto.ProfileResponseDto;
import woozlabs.echo.domain.member.service.AccountService;

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
}
