package woozlabs.echo.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.member.dto.profile.AccountProfileResponseDto;
import woozlabs.echo.domain.member.service.AccountService;

@RestController
@RequestMapping("/api/v1/echo/user")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/profile")
    public ResponseEntity<AccountProfileResponseDto> getProfileByEmail(@RequestParam String email) {
        AccountProfileResponseDto response = accountService.getProfileByField("email", email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uid}/profile")
    public ResponseEntity<AccountProfileResponseDto> getProfileByUid(@PathVariable("uid") String uid) {
        AccountProfileResponseDto response = accountService.getProfileByField("uid", uid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{uid}/unlink")
    public ResponseEntity<Void> unlinkAccountFromMember(@PathVariable("uid") String uid,
                                                        @RequestParam("accountUid") String accountUid) {
        accountService.unlinkAccount(uid, accountUid);
        return ResponseEntity.noContent().build();
    }
}
