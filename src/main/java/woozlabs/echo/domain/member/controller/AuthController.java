package woozlabs.echo.domain.member.controller;

import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.member.service.AuthService;
import woozlabs.echo.global.aop.annotations.VerifyToken;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/google/callback")
    public void handleOAuthCallback(@RequestParam("code") String code,
                                    HttpServletRequest request,
                                    HttpServletResponse response) throws FirebaseAuthException {
        authService.handleGoogleCallback(code, request, response);
    }

    @GetMapping("/verify-token")
    @VerifyToken
    public ResponseEntity<String> testVerify() {
        return ResponseEntity.ok("Token is valid");
    }
}
