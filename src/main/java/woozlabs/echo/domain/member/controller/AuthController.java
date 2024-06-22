package woozlabs.echo.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.member.dto.CreateAccountRequestDto;
import woozlabs.echo.domain.member.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/create-account")
    public ResponseEntity<Void> createAccount(@RequestBody CreateAccountRequestDto requestDto) {
        authService.createAccount(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
