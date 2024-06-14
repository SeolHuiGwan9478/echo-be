package woozlabs.echo.global.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import woozlabs.echo.global.security.GoogleOauthMemberDetails;
import woozlabs.echo.global.security.provider.JwtProvider;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Value("${jwt.access_token_expiration}")
    private Long accessTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {

        GoogleOauthMemberDetails oauth2User = (GoogleOauthMemberDetails) authentication.getPrincipal();
        String userEmail = oauth2User.getName();
        String accessToken = jwtProvider.generateToken(userEmail, accessTokenExpiration);

        response.addHeader("Authorization", "Bearer " + accessToken);

        response.sendRedirect("http://localhost:8080"); // Redirect URL 보류 | 240614 ldhbenecia
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
