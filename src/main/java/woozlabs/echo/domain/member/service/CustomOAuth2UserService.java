package woozlabs.echo.domain.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.Role;
import woozlabs.echo.domain.member.entity.SubAccount;
import woozlabs.echo.domain.member.entity.SuperAccount;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.member.repository.SubAccountRepository;
import woozlabs.echo.domain.member.repository.SuperAccountRepository;
import woozlabs.echo.global.security.GoogleOauthMemberDetails;
import woozlabs.echo.global.security.provider.JwtProvider;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final SuperAccountRepository superAccountRepository;
    private final SubAccountRepository subAccountRepository;
    private final JwtProvider jwtProvider;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @Value("${jwt.access_token_expiration}")
    private Long accessTokenExpiration;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String oauthClientName = userRequest.getClientRegistration().getClientName();
        String googleAccessToken = userRequest.getAccessToken().getTokenValue();

        try {
            log.info("OAuth2 User Attributes: {}", new ObjectMapper().writeValueAsString(oAuth2User.getAttributes()));
        } catch (Exception exception) {
            log.error("Failed to serialize OAuth2User: {}", exception.getMessage());
        }

        if (oauthClientName.equals("Google")) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String providerId = attributes.get("sub").toString();
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String profileImage = oAuth2User.getAttribute("picture");

            String superToken = getCookieValue(request, "superToken");
            if (superToken == null || !jwtProvider.validateToken(superToken)) {
                // 첫 번째 로그인인 경우 Super Account 등록
                SuperAccount superAccount = superAccountRepository.findByEmail(email);
                if (superAccount == null) {
                    String superAccountJwtToken = jwtProvider.generateToken(email, accessTokenExpiration);
                    superAccount = createSuperAccountFromOAuth2User(oAuth2User, providerId, googleAccessToken, superAccountJwtToken);
                }

                // Super 토큰 생성 후 쿠키에 저장
                superToken = jwtProvider.generateToken(email, accessTokenExpiration);
                Cookie superTokenCookie = new Cookie("superToken", superToken);
                superTokenCookie.setHttpOnly(true);
                superTokenCookie.setPath("/");
                superTokenCookie.setMaxAge(-1); // 만료기한 무제한으로 설정 | 241618 ldhbenecia
                response.addCookie(superTokenCookie);
            } else {
                String superEmail = jwtProvider.getUsernameFromToken(superToken);
                Optional<Member> superMember = memberRepository.findByEmail(superEmail);
                if (superMember.isPresent() && !superEmail.equals(email)) {
                    String subAccountJwtToken = jwtProvider.generateToken(email, accessTokenExpiration);

                    SubAccount subAccount = SubAccount.builder()
                            .superAccount(superMember.get().getSuperAccount())
                            .email(email)
                            .name(name)
                            .role(Role.ROLE_USER)
                            .provider("google")
                            .providerId(providerId)
                            .profileImage(profileImage)
                            .JwtToken(subAccountJwtToken)
                            .googleAccessToken(googleAccessToken)
                            .build();

                    subAccountRepository.save(subAccount);
                }
            }
            Member member = memberRepository.findByEmail(email)
                    .orElseGet(() -> createUserFromOAuth2User(oAuth2User, providerId));

            return new GoogleOauthMemberDetails(member, attributes);
        }

        return oAuth2User;
    }

    private Member createUserFromOAuth2User(OAuth2User oAuth2User, String providerId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = attributes.get("email").toString();
        String name = attributes.get("name").toString();
        String profileImage = attributes.get("picture").toString();

        Optional<Member> optionalMember = Optional.ofNullable(memberRepository.findByProviderAndProviderId("google", providerId));
        if (optionalMember.isPresent()) {
            return optionalMember.get();
        } else {
            Member member = Member.builder()
                    .email(email)
                    .name(name)
                    .role(Role.ROLE_USER)
                    .provider("google")
                    .providerId(providerId)
                    .profileImage(profileImage)
                    .build();

            return memberRepository.save(member);
        }
    }

    private SuperAccount createSuperAccountFromOAuth2User(OAuth2User oAuth2User, String providerId, String googleAccessToken, String superAccountJwtToken) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = attributes.get("email").toString();
        String name = attributes.get("name").toString();
        String profileImage = attributes.get("picture").toString();

        Optional<Member> optionalMember = Optional.ofNullable(memberRepository.findByProviderAndProviderId("google", providerId));
        Member member;
        if (optionalMember.isPresent()) {
            member = optionalMember.get();
        } else {
            member = Member.builder()
                    .email(email)
                    .name(name)
                    .role(Role.ROLE_USER)
                    .provider("google")
                    .providerId(providerId)
                    .profileImage(profileImage)
                    .build();
            memberRepository.save(member);
        }

        SuperAccount superAccount = SuperAccount.builder()
                .email(email)
                .name(name)
                .role(Role.ROLE_USER)
                .provider("google")
                .providerId(providerId)
                .profileImage(profileImage)
                .googleAccessToken(googleAccessToken)
                .JwtToken(superAccountJwtToken)
                .member(member)
                .build();
        return superAccountRepository.save(superAccount);
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
