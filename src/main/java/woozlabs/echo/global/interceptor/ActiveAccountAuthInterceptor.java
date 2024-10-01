package woozlabs.echo.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import woozlabs.echo.global.constant.GlobalConstant;

import static woozlabs.echo.global.constant.GlobalConstant.AUTH_UNAUTHORIZED_ERR_MSG;

@Component
@RequiredArgsConstructor
public class ActiveAccountAuthInterceptor implements HandlerInterceptor {
    private final String ACTIVE_ACCOUNT_UID_HEADER = "Aa-Uid";
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestMethod = request.getMethod();
        if(requestMethod.equals("OPTIONS")){
            return true;
        }
        String activeAccountUid = request.getHeader(ACTIVE_ACCOUNT_UID_HEADER);
        if(activeAccountUid == null){ // token checking
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, AUTH_UNAUTHORIZED_ERR_MSG);
            return false;
        }
        request.setAttribute(GlobalConstant.ACTIVE_ACCOUNT_UID_KEY, activeAccountUid);
        return true;
    }
}
