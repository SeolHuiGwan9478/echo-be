package woozlabs.echo.global.interceptor;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.utils.FirebaseTokenVerifier;

import java.util.Arrays;
import java.util.List;

import static woozlabs.echo.global.constant.GlobalConstant.*;

@Component
@RequiredArgsConstructor
public class FirebaseAuthInterceptor implements HandlerInterceptor {
    private final String AUTH_HEADER_NAME = "Authorization";
    private final String AUTH_HEADER_PREFIX = "Bearer ";
    private final String FIREBASE_UID_KEY = "uid";
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final List<String> optionalTokenPaths = Arrays.asList("/api/v1/echo/public-share/");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();

        if ("GET".equalsIgnoreCase(requestMethod) && optionalTokenPaths.stream().anyMatch(requestPath::startsWith)) {
            String authHeader = request.getHeader(AUTH_HEADER_NAME);
            if (authHeader != null && authHeader.startsWith(AUTH_HEADER_PREFIX)) {
                String idToken = authHeader.replace(AUTH_HEADER_PREFIX, EMPTY_CHAR);
                try {
                    FirebaseToken firebaseToken = firebaseTokenVerifier.verifyToken(idToken);
                    request.setAttribute(FIREBASE_UID_KEY, firebaseToken.getUid());
                } catch (FirebaseAuthException e) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, AUTH_UNAUTHORIZED_ERR_MSG);
                    return false;
                }
            }
            return true;
        }

        if(request.getMethod().equals("OPTIONS")){
            return true;
        }
        String authHeader = request.getHeader(AUTH_HEADER_NAME);
        if(authHeader == null || !authHeader.startsWith(AUTH_HEADER_PREFIX)){ // token checking
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, AUTH_UNAUTHORIZED_ERR_MSG);
            return false;
        }
        String idToken = authHeader.replace(AUTH_HEADER_PREFIX, EMPTY_CHAR);
        FirebaseToken firebaseToken;
        try{
            firebaseToken = firebaseTokenVerifier.verifyToken(idToken);
        }catch (FirebaseAuthException e){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, AUTH_UNAUTHORIZED_ERR_MSG);
            return false;
        }        // setting attribute of request obj
        request.setAttribute(GlobalConstant.FIREBASE_UID_KEY, firebaseToken.getUid());
        return true;
    }
}
