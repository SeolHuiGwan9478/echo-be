package woozlabs.echo.domain.member.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;

public class AuthCookieUtils {

    public static void addCookie(HttpServletResponse response, String uid) {
        Cookie cookie = new Cookie("super_account", uid);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
    }

    public static Optional<String> getCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "member_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
