package woozlabs.echo.domain.member.controller;

import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.member.dto.AddAccountRequestDto;
import woozlabs.echo.domain.member.dto.SignInRequestDto;
import woozlabs.echo.domain.member.dto.GoogleResponseDto;
import woozlabs.echo.domain.member.service.AuthService;
import woozlabs.echo.global.aop.annotations.VerifyToken;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/google/callback")
    public void handleOAuthCallback(@RequestParam("code") String code,
                                    @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                    HttpServletResponse response) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String idToken = authorizationHeader.replace("Bearer ", "");
                authService.addAccount(code, idToken, response);
            } else {
                authService.signIn(code, response);
            }
        } catch (FirebaseAuthException e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_VERIFY_ID_TOKEN);
        }
    }

    @GetMapping("/verify-token")
    @VerifyToken
    public ResponseEntity<String> testVerify() {
        return ResponseEntity.ok("Token is valid");
    }
}
