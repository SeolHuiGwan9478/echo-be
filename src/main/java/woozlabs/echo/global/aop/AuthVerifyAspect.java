package woozlabs.echo.global.aop;

import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;
import woozlabs.echo.global.utils.FirebaseTokenVerifier;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthVerifyAspect {

    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final HttpServletRequest request;

    @Before("@annotation(woozlabs.echo.global.aop.annotations.VerifyToken)")
    public void verifyToken() throws FirebaseAuthException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomErrorException(ErrorCode.NOT_FOUND_VERIFY_TOKEN);
        }

        String idToken = authorizationHeader.replace("Bearer ", "");
        try {
            firebaseTokenVerifier.verifyToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new CustomErrorException(ErrorCode.NOT_VERIFY_ID_TOKEN);
        }
    }
}
