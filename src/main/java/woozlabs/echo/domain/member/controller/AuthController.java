package woozlabs.echo.domain.member.controller;

import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.member.dto.AddAccountRequestDto;
import woozlabs.echo.domain.member.dto.CreateAccountRequestDto;
import woozlabs.echo.domain.member.service.AuthService;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

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

    @PostMapping("/add-account")
    public ResponseEntity<Void> addAccount(@RequestHeader("Authorization") String authorizationHeader,
                                           @RequestBody AddAccountRequestDto requestDto) {
        try {
            String idToken = authorizationHeader.replace("Bearer ", "");
            authService.addAccount(idToken, requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (FirebaseAuthException e) {
            throw new CustomErrorException(ErrorCode.NOT_VERIFY_ID_TOKEN);
        }
    }
}
